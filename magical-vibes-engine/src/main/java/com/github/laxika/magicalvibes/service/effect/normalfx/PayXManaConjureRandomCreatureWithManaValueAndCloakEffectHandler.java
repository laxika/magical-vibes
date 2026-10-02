package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.condition.SourceIsFaceDown;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaConjureRandomCreatureWithManaValueAndCloakEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceUpEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Roalesk's optional X payment and cloaked conjure. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaConjureRandomCreatureWithManaValueAndCloakEffectHandler
        implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final Map<Integer, List<CardPrinting>> candidatesByManaValue = new ConcurrentHashMap<>();

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaConjureRandomCreatureWithManaValueAndCloakEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;
            if (chosenValue == 0) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " chooses X=0 for " + cardName + "'s ability."));
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (payableFromPool(pool) < chosenValue) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                beginXPrompt(gameData, controllerId, cardName);
                return;
            }

            new ManaCost("{X}").pay(pool, chosenValue);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays {" + chosenValue + "} for " + cardName + "."));
            conjureAndCloak(gameData, entry, chosenValue);
            return;
        }

        if (maxPotentialX(gameData, controllerId) <= 0) {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " has no mana to pay for ", entry.getCard(), "'s ability."));
            return;
        }
        beginXPrompt(gameData, controllerId, cardName);
    }

    private void conjureAndCloak(GameData gameData, StackEntry entry, int manaValue) {
        List<CardPrinting> candidates = candidatesByManaValue.computeIfAbsent(
                manaValue, this::findCandidates);
        if (candidates.isEmpty()) {
            return;
        }

        Card card = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).createCard();
        card.setOwnerId(entry.getControllerId());
        Permanent cloaked = new Permanent(card);
        cloaked.setFaceDownAsCloaked();
        cloaked.getPersistentGrantedActivatedAbilities().add(new ActivatedAbility(
                false,
                "{G/U}",
                List.of(new TurnSourceFaceUpEffect()),
                "{G/U}: Turn this creature face up.")
                .withActivationCondition(new SourceIsFaceDown(),
                        "Activate only while this creature is face down."));
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), cloaked);
        battlefieldEntryService.processFaceDownCreatureETBTriggers(
                gameData, entry.getControllerId(), card);
        entry.getCreatedPermanentIds().add(cloaked.getId());

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(entry.getControllerId()) + " conjures and cloaks a creature."));
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(
                        controllerId,
                        maxX,
                        "Pay {X} for " + cardName + "? Conjure and cloak a random creature with mana value X.",
                        cardName,
                        true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - pool.getTotal();
        return payableFromPool(pool) + untappedSources;
    }

    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless()
                + pool.getMyrOnlyColorless() + pool.getXCostOnlyColorless();
    }

    private List<CardPrinting> findCandidates(int manaValue) {
        Map<String, CardPrinting> uniquePrintings = new LinkedHashMap<>();
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(set)) {
                uniquePrintings.putIfAbsent(printing.cardClassName(), printing);
            }
        }
        return uniquePrintings.values().stream()
                .filter(printing -> {
                    Card card = printing.createCard();
                    return card.hasType(CardType.CREATURE) && card.getManaValue() == manaValue;
                })
                .toList();
    }
}
