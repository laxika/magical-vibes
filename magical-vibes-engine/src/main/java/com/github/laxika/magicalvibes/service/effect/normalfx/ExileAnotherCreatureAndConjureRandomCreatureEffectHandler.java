package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnotherCreatureAndConjureRandomCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Fear of Change's exile-and-random-conjure trigger. */
@Component
@RequiredArgsConstructor
public class ExileAnotherCreatureAndConjureRandomCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAnotherCreatureAndConjureRandomCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = sourcePermanentId(gameData, entry);
        List<UUID> eligibleIds = eligibleCreatureIds(gameData, controllerId, sourcePermanentId);
        if (eligibleIds.isEmpty()) {
            return;
        }
        if (eligibleIds.size() == 1) {
            exileAndConjure(gameData, eligibleIds.getFirst(), controllerId, entry.getCard());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ExileAnotherCreatureAndConjureRandomCreature(
                        entry.getCard(), sourcePermanentId, controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, eligibleIds,
                entry.getCard().getName() + " — choose another creature to exile.");
    }

    public void completePermanentChoice(GameData gameData, UUID permanentId,
                                        PermanentChoiceContext.ExileAnotherCreatureAndConjureRandomCreature context) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || permanentId.equals(context.sourcePermanentId())
                || !gameQueryService.isCreature(gameData, permanent)) {
            return;
        }
        exileAndConjure(gameData, permanentId, context.controllerId(), context.sourceCard());
    }

    private void exileAndConjure(GameData gameData, UUID permanentId, UUID controllerId,
                                 Card sourceCard) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null) {
            return;
        }
        int targetManaValue = 2 + chosen.getCard().getManaValue();
        Card exiledCard = chosen.getCard();
        if (!permanentRemovalService.removePermanentToExile(gameData, chosen)) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData,
                GameLog.cardTextCard(exiledCard, " is exiled by ", sourceCard, "."));

        List<CardPrinting> candidates = creaturePrintingsAtManaValue(targetManaValue);
        if (candidates.isEmpty()) {
            return;
        }
        Card conjuredCard = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).createCard();
        Permanent conjuredPermanent = new Permanent(conjuredCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, conjuredPermanent);
        if (gameQueryService.findPermanentById(gameData, conjuredPermanent.getId()) == null) {
            return;
        }
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                gameData, controllerId, conjuredPermanent, conjuredCard);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " conjures ", conjuredCard,
                " onto the battlefield."));
    }

    private List<UUID> eligibleCreatureIds(GameData gameData, UUID controllerId,
                                            UUID sourcePermanentId) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(controllerId, List.of());
        return battlefield.stream()
                .filter(permanent -> !permanent.getId().equals(sourcePermanentId))
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    private List<CardPrinting> creaturePrintingsAtManaValue(int manaValue) {
        List<CardPrinting> candidates = new ArrayList<>();
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(set)) {
                Card sample = printing.createCard();
                if (sample.hasType(CardType.CREATURE) && sample.getManaValue() == manaValue) {
                    candidates.add(printing);
                }
            }
        }
        return candidates;
    }

    private UUID sourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        if (entry.getSourcePermanentSnapshot() != null) {
            return entry.getSourcePermanentSnapshot().getId();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of());
        return battlefield.stream()
                .filter(permanent -> permanent.getCard() == entry.getCard()
                        || (permanent.getCard() != null && entry.getCard() != null
                        && permanent.getCard().getId().equals(entry.getCard().getId())))
                .map(Permanent::getId)
                .findFirst()
                .orElse(null);
    }
}
