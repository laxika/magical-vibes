package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaReanimateCreatureWithManaValueXEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link PayXManaReanimateCreatureWithManaValueXEffect}: prompt for X, pay it, then put the
 * reflexive "return target creature card with mana value X from your graveyard to the battlefield"
 * trigger on the stack with its target already chosen (Isareth the Awakener).
 *
 * <p>The reflexive trigger is a normal pre-targeted {@link ReturnCardFromGraveyardEffect}, so the
 * corpse counter and the exile-instead-of-leaving replacement ride along on the existing graveyard
 * return path.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaReanimateCreatureWithManaValueXEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaReanimateCreatureWithManaValueXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayXManaReanimateCreatureWithManaValueXEffect) effect;
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String cardName = sourceCard.getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Re-entry after the player chose X
        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameLogService.append(gameData, GameLog.text(playerName + " chooses X=0 for " + cardName + "'s ability."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            // Cap was based on potential mana so the player could tap lands during the
            // prompt; re-check the actual pool before charging.
            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (payableFromPool(pool) < chosenValue) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                log.info("Game {} - {} cannot yet pay X={} for {} — re-prompting",
                        gameData.id, playerName, chosenValue, cardName);
                beginXPrompt(gameData, controllerId, cardName);
                return;
            }
            new ManaCost("{X}").pay(pool, chosenValue);

            gameLogService.append(gameData, GameLog.text(playerName + " pays {" + chosenValue + "} for " + cardName + "."));
            log.info("Game {} - {} pays {} mana for {}", gameData.id, playerName, chosenValue, cardName);
            beginReflexiveTrigger(gameData, entry, e, controllerId, chosenValue);
            return;
        }

        // First call: cap includes untapped mana sources so an empty pool with untapped
        // lands still opens the prompt (CR 605.3a — mana abilities during the payment).
        if (maxPotentialX(gameData, controllerId) <= 0) {
            gameLogService.append(gameData, GameLog.textCardText(playerName + " has no mana to pay for ", sourceCard, "'s ability."));
            log.info("Game {} - {} has no mana for {}'s pay-X reanimate ability", gameData.id, playerName, cardName);
            return;
        }
        beginXPrompt(gameData, controllerId, cardName);
    }

    /**
     * Puts the reflexive trigger on the stack with a creature card of mana value {@code x} from the
     * controller's graveyard as its target. A single legal card is targeted without a prompt; several
     * let the controller pick one; none means the trigger simply does not go on the stack.
     */
    private void beginReflexiveTrigger(GameData gameData, StackEntry entry,
                                       PayXManaReanimateCreatureWithManaValueXEffect e,
                                       UUID controllerId, int x) {
        Card sourceCard = entry.getCard();
        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(manaValueCreatureFilter(x))
                .targetGraveyard(true)
                .enterWithCounter(e.enterWithCounter())
                .enterWithCounterCount(e.enterWithCounter() == null ? 0 : 1)
                .exileIfLeavesBattlefield(e.exileIfLeavesBattlefield())
                .build();

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        List<Integer> matchingIndices = new ArrayList<>();
        if (graveyard != null) {
            for (int i = 0; i < graveyard.size(); i++) {
                if (predicateEvaluationService.matchesCardPredicate(
                        graveyard.get(i), returnEffect.filter(), sourceCard.getId())) {
                    matchingIndices.add(i);
                }
            }
        }

        if (matchingIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    "'s ability has no creature card with mana value " + x + " to target."));
            log.info("Game {} - {} reflexive reanimate trigger has no legal target (X={})",
                    gameData.id, sourceCard.getName(), x);
            return;
        }

        String description = sourceCard.getName() + "'s ability";
        if (matchingIndices.size() == 1) {
            Card targetCard = graveyard.get(matchingIndices.getFirst());
            // Zone-aware entry: the target lives in the graveyard, so CR 608.2b legality on
            // resolution must look there rather than on the battlefield.
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, controllerId, description,
                    new ArrayList<>(List.of(returnEffect)), x, targetCard.getId(),
                    entry.getSourcePermanentId(), null, Zone.GRAVEYARD, null, null));
            gameLogService.append(gameData, GameLog.builder().card(sourceCard)
                    .text("'s ability targets ").card(targetCard).text(" in graveyard.").build());
            return;
        }

        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, matchingIndices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        "Choose a creature card with mana value " + x + " to return to the battlefield.")
                .mayAbilityContext(sourceCard, controllerId, List.of(returnEffect), entry.getSourcePermanentId())
                .build());
    }

    /** "Creature card with mana value X" — an exact mana value expressed as at-most AND at-least X. */
    private static CardPredicate manaValueCreatureFilter(int x) {
        return new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMaxManaValuePredicate(x),
                new CardMinManaValuePredicate(x)));
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        String prompt = "Pay {X} for " + cardName + "? Return a creature card with mana value X from your graveyard.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, cardName, true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - gameData.playerManaPools.get(controllerId).getTotal();
        return payableFromPool(gameData.playerManaPools.get(controllerId)) + untappedSources;
    }

    /** Generic-payable mana in the pool right now — mirrors what {@code pay} can drain. */
    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless()
                + pool.getMyrOnlyColorless() + pool.getXCostOnlyColorless();
    }
}
