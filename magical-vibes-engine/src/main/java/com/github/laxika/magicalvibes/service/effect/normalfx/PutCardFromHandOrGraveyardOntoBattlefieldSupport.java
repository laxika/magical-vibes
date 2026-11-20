package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardAttachedToPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Component
@RequiredArgsConstructor
public class PutCardFromHandOrGraveyardOntoBattlefieldSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            UUID sourceCardId, String sourceCardName) {
        beginChoice(gameData, playerId, predicate, label, sourceCardId, sourceCardName,
                null, 0, false, false, true, false, null, null, false);
    }

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            UUID sourceCardId, String sourceCardName, CounterType enterWithCounter) {
        beginChoice(gameData, playerId, predicate, label, sourceCardId, sourceCardName,
                enterWithCounter, enterWithCounter == null ? 0 : 1, false, false,
                true, false, null, null, false);
    }

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            UUID sourceCardId, String sourceCardName, CounterType enterWithCounter,
                            boolean grantHaste, boolean returnToHandAtEndStep) {
        beginChoice(gameData, playerId, predicate, label, sourceCardId, sourceCardName,
                enterWithCounter, enterWithCounter == null ? 0 : 1, grantHaste, returnToHandAtEndStep,
                true, false, null, null, false);
    }

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            UUID sourceCardId, String sourceCardName, CounterType enterWithCounter,
                            int enterWithCounterCount, boolean grantHaste, boolean returnToHandAtEndStep) {
        beginChoice(gameData, playerId, predicate, label, sourceCardId, sourceCardName,
                enterWithCounter, enterWithCounterCount, grantHaste, returnToHandAtEndStep,
                true, false, null, null, false);
    }

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            UUID sourceCardId, String sourceCardName, CounterType enterWithCounter,
                            int enterWithCounterCount, boolean grantHaste, boolean returnToHandAtEndStep,
                            boolean includeGraveyard, boolean includeCommandZone,
                            UUID delayedAuraCardId, UUID delayedAuraControllerId,
                            boolean handleCreatureEtbAndLegendRule) {
        List<UUID> validCardIds = new ArrayList<>();
        addMatchingCardIds(validCardIds, gameData.playerHands.get(playerId), predicate,
                sourceCardId, gameData, playerId);
        if (includeGraveyard) {
            addMatchingCardIds(validCardIds, gameData.playerGraveyards.get(playerId), predicate,
                    sourceCardId, gameData, playerId);
        }
        if (includeCommandZone) {
            addMatchingCardIds(validCardIds, gameData.playerCommandZones.get(playerId), predicate,
                    sourceCardId, gameData, playerId);
        }
        if (validCardIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PutCardFromHandOrGraveyardChoice(
                        playerId, validCardIds, label, sourceCardName, enterWithCounter, enterWithCounterCount,
                        grantHaste, returnToHandAtEndStep, includeGraveyard, includeCommandZone,
                        delayedAuraCardId, delayedAuraControllerId, handleCreatureEtbAndLegendRule));
    }

    public void applyChoice(GameData gameData, UUID playerId, UUID chosenCardId, String sourceCardName) {
        applyChoice(gameData, playerId, chosenCardId, sourceCardName, null,
                0, false, false, true, false, null, null, false);
    }

    public void applyChoice(GameData gameData, UUID playerId, UUID chosenCardId, String sourceCardName,
                            CounterType enterWithCounter) {
        applyChoice(gameData, playerId, chosenCardId, sourceCardName,
                enterWithCounter, enterWithCounter == null ? 0 : 1, false, false,
                true, false, null, null, false);
    }

    public void applyChoice(GameData gameData, UUID playerId, UUID chosenCardId, String sourceCardName,
                            CounterType enterWithCounter, boolean grantHaste,
                            boolean returnToHandAtEndStep) {
        applyChoice(gameData, playerId, chosenCardId, sourceCardName,
                enterWithCounter, enterWithCounter == null ? 0 : 1, grantHaste, returnToHandAtEndStep,
                true, false, null, null, false);
    }

    public void applyChoice(GameData gameData, UUID playerId, UUID chosenCardId, String sourceCardName,
                            CounterType enterWithCounter, int enterWithCounterCount,
                            boolean grantHaste, boolean returnToHandAtEndStep) {
        applyChoice(gameData, playerId, chosenCardId, sourceCardName,
                enterWithCounter, enterWithCounterCount, grantHaste, returnToHandAtEndStep,
                true, false, null, null, false);
    }

    public void applyChoice(GameData gameData, UUID playerId, UUID chosenCardId, String sourceCardName,
                            CounterType enterWithCounter, int enterWithCounterCount,
                            boolean grantHaste, boolean returnToHandAtEndStep,
                            boolean includeGraveyard, boolean includeCommandZone,
                            UUID delayedAuraCardId, UUID delayedAuraControllerId,
                            boolean handleCreatureEtbAndLegendRule) {
        Card chosen = removeCard(gameData.playerHands.get(playerId), chosenCardId);
        String zone = "hand";
        if (chosen == null && includeGraveyard) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            chosen = removeCard(graveyard, chosenCardId);
            zone = "graveyard";
            if (chosen != null) {
                graveyardService.notifyCardsLeftGraveyard(gameData, playerId, chosen);
            }
        }
        if (chosen == null && includeCommandZone) {
            chosen = removeCard(gameData.playerCommandZones.get(playerId), chosenCardId);
            zone = "command zone";
        }
        if (chosen == null) {
            throw new IllegalStateException("Chosen card is no longer in an eligible zone");
        }

        Permanent permanent = "command zone".equals(zone)
                ? new Permanent(chosen, Zone.COMMAND)
                : new Permanent(chosen);
        if (grantHaste) {
            gameData.addEffectOnPermanent(sourceCardName, null, playerId, new GrantKeywordEffect(Keyword.HASTE, Subjects.target()), permanent.getId(), Duration.UNTIL_END_OF_TURN);
        }
        EnterWithCountersEffect enterWithCounters = enterWithCounter != null && enterWithCounterCount > 0
                ? new EnterWithCountersEffect(enterWithCounter, new Fixed(enterWithCounterCount))
                : null;
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent,
                battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of(), enterWithCounters);
        if (handleCreatureEtbAndLegendRule) {
            graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, playerId, permanent, chosen);
        }
        if (returnToHandAtEndStep) {
            DelayedPermanentActions.scheduleForResolution(gameData, playerId, permanent,
                    new DelayedPermanentAction(permanent.getId(),
                            DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP),
                    ActivePlayer.ANY);
        }
        if (delayedAuraCardId != null && delayedAuraControllerId != null) {
            registerAuraReturn(gameData, delayedAuraCardId, delayedAuraControllerId, permanent.getId());
        }
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " puts "
                + chosen.getName() + " from their " + zone + " onto the battlefield ("
                + sourceCardName + ")."));
    }

    /**
     * The delayed triggered ability "return {@code auraCardId} to the battlefield attached to that creature at the
     * beginning of the next end step" (Next of Kin) that its controller {@code controllerId} made as the creature
     * {@code enchantedPermanentId} entered. The Aura card is the one in a graveyard now (CR 400.7), and comes back
     * under {@code controllerId}'s control (CR 110.2a).
     */
    private void registerAuraReturn(GameData gameData, UUID auraCardId, UUID controllerId,
                                    UUID enchantedPermanentId) {
        for (UUID graveyardOwnerId : gameData.orderedPlayerIds) {
            for (Card aura : gameData.playerGraveyards.getOrDefault(graveyardOwnerId, List.of())) {
                if (aura.getId().equals(auraCardId)) {
                    gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                            controllerId, aura,
                            new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                                    new ReturnAuraFromGraveyardAttachedToPermanentEffect(
                                            auraCardId, controllerId, enchantedPermanentId)),
                            null, null, null,
                            DelayedReferents.graveyardCard(auraCardId, gameData.graveyardEntryVersion(auraCardId))));
                    return;
                }
            }
        }
    }

    private void addMatchingCardIds(List<UUID> destination, List<Card> cards, CardPredicate predicate,
                                    UUID sourceCardId, GameData gameData, UUID playerId) {
        if (cards == null) {
            return;
        }
        for (Card card : cards) {
            if (predicateEvaluationService.matchesCardPredicate(card, predicate, sourceCardId,
                    gameData, playerId)) {
                destination.add(card.getId());
            }
        }
    }

    private Card removeCard(List<Card> cards, UUID cardId) {
        if (cards == null) {
            return null;
        }
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            if (card.getId().equals(cardId)) {
                cards.remove(i);
                return card;
            }
        }
        return null;
    }
}
