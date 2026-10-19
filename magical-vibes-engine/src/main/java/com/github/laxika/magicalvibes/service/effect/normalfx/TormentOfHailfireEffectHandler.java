package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TormentState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TormentOfHailfireEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TormentOfHailfireEffect}: "Repeat the following process N times. Each opponent
 * loses lifeLoss life unless that player sacrifices a matching permanent of their choice or discards
 * a card." {@code N} is {@link TormentOfHailfireEffect#fixedIterations()} when non-null, otherwise the
 * stack entry's {@code xValue}. The effect's optional predicate narrows the sacrifice choice; when
 * absent, any nonland permanent is eligible.
 *
 * <p>The flow is driven one opponent at a time and re-runs on every choice completion (kept alive via
 * {@link GameData#rerunCurrentEffectAfterInteraction}), mirroring
 * {@link EachPlayerMayDiscardUpToThenTakeDamageEffectHandler}. Progress lives on {@link GameData#torment}:
 * {@code remainingIterations} counts whole passes still to do; {@code remaining} is the APNAP opponent
 * queue for the current pass. For each opponent the offered options are pruned to what they can do (life
 * is always offered); an opponent whose only option is to lose life takes it immediately without a prompt.
 * Otherwise a three-way {@link PendingInteraction.ColorChoice} list-pick is begun; the chosen mode then
 * runs a sacrifice ({@link PermanentChoiceContext.TormentSacrifice}) or discard sub-choice, or applies
 * the life loss inline. Each sub-choice completion re-runs this handler to advance to the next opponent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TormentOfHailfireEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;
    private final DestructionSupport destructionSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Autowired
    @Lazy
    private CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TormentOfHailfireEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TormentOfHailfireEffect torment = (TormentOfHailfireEffect) effect;
        int lifeLoss = torment.lifeLoss();
        int discardCount = torment.discardCount();
        TormentState state = gameData.torment;
        String sourceName = entry.getCard().getName();

        if (!state.active) {
            // Fresh entry: seed the iteration counter and start processing.
            state.reset();
            state.active = true;
            int iterations;
            if (torment.fixedIterations() != null) {
                iterations = torment.fixedIterations();
            } else if (torment.dynamicIterations() != null) {
                var source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                iterations = amountEvaluationService.evaluate(gameData, torment.dynamicIterations(),
                        AmountContext.forStackEntry(entry, source));
            } else {
                iterations = entry.getXValue();
            }
            state.remainingIterations = Math.max(0, iterations);
            if (torment.deferChoices()) {
                // Each opponent makes all of their choices in turn order, then everything happens together.
                state.deferred = true;
                state.totalPasses = state.remainingIterations;
                state.remainingIterations = 0;
                state.remaining.addAll(torment.targeted()
                        ? targetedPlayer(gameData, entry.getTargetId())
                        : apnapOpponents(gameData, entry.getControllerId()));
            }
            if (!torment.deferChoices() && torment.deferEachIteration() && state.remainingIterations > 0) {
                // Every opponent chooses before the penalties of this repetition are applied; the next
                // repetition starts once they have been.
                state.deferred = true;
                state.deferEachIteration = true;
                state.targetedFlow = torment.targeted();
                state.totalPasses = 1;
                state.remainingIterations--;
                state.remaining.addAll(torment.targeted()
                        ? targetedPlayer(gameData, entry.getTargetId())
                        : apnapOpponents(gameData, entry.getControllerId()));
            }
            advance(gameData, entry, sourceName, lifeLoss, discardCount,
                    torment.sacrificePredicate(), torment.targeted());
            return;
        }

        if (state.chosenMode != null) {
            // The current opponent just picked a penalty option — apply it.
            String mode = state.chosenMode;
            state.chosenMode = null;
            if (state.deferred) {
                applyDeferredMode(gameData, entry, sourceName, lifeLoss, discardCount,
                        state.currentOpponentId, mode, torment.sacrificePredicate());
                return;
            }
            applyMode(gameData, entry, sourceName, lifeLoss, discardCount,
                    state.currentOpponentId, mode, torment.sacrificePredicate(), torment.targeted());
            return;
        }

        // Re-entry after a discard / sacrifice sub-choice completed — advance to the next opponent.
        gameData.deferredDiscardChooserId = null;
        advance(gameData, entry, sourceName, lifeLoss, discardCount,
                torment.sacrificePredicate(), torment.targeted());
    }

    /**
     * Processes opponents until a penalty choice must be prompted (then returns, pausing resolution)
     * or the whole X-times process finishes. Opponents whose only option is to lose life take it
     * inline without a prompt; each new iteration refills the APNAP opponent queue.
     */
    private void advance(GameData gameData, StackEntry entry, String sourceName, int lifeLoss,
            int discardCount, PermanentPredicate sacrificePredicate, boolean targeted) {
        TormentState state = gameData.torment;
        UUID controllerId = entry.getControllerId();
        if (state.deferred) {
            advanceDeferred(gameData, entry, sourceName, lifeLoss, discardCount, sacrificePredicate);
            return;
        }
        while (true) {
            if (state.remaining.isEmpty()) {
                if (state.remainingIterations <= 0) {
                    gameData.rerunCurrentEffectAfterInteraction = false;
                    state.reset();
                    return;
                }
                state.remainingIterations--;
                state.remaining.addAll(targeted
                        ? targetedPlayer(gameData, entry.getTargetId())
                        : apnapOpponents(gameData, controllerId));
                if (state.remaining.isEmpty()) {
                    // No opponents to process this iteration.
                    continue;
                }
            }

            UUID opponentId = state.remaining.pollFirst();
            if (!gameData.playerIds.contains(opponentId)) {
                continue;
            }
            state.currentOpponentId = opponentId;

            List<String> options = availableOptions(gameData, opponentId, controllerId, lifeLoss,
                    discardCount, sacrificePredicate);
            if (options.size() == 1) {
                // Only "lose life" is possible — no choice to make.
                lifeSupport.applyLifeLoss(gameData, opponentId, lifeLoss, sourceName);
                continue;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            String prompt = sourceName + " — lose " + lifeLoss
                    + " life unless you sacrifice " + sacrificeDescription(sacrificePredicate)
                    + " or discard " + discardDescription(discardCount) + ".";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    opponentId, null, null,
                    new ChoiceContext.TormentPenaltyChoice(opponentId, sourceName),
                    options, prompt));
            return;
        }
    }

    /**
     * Deferred form: opponents in turn order each make all of their choices (never choosing the same
     * permanent or card twice), then the sacrifices, discards and life loss all happen together.
     */
    private void advanceDeferred(GameData gameData, StackEntry entry, String sourceName, int lifeLoss,
            int discardCount, PermanentPredicate sacrificePredicate) {
        TormentState state = gameData.torment;
        UUID controllerId = entry.getControllerId();
        while (true) {
            if (state.opponentPassesLeft <= 0) {
                UUID next = state.remaining.pollFirst();
                if (next == null) {
                    finishDeferred(gameData, entry, sourceName, lifeLoss, discardCount, sacrificePredicate);
                    return;
                }
                if (gameData.playerIds.contains(next)) {
                    state.currentOpponentId = next;
                    state.opponentPassesLeft = state.totalPasses;
                }
                continue;
            }
            state.opponentPassesLeft--;
            UUID opponentId = state.currentOpponentId;
            List<String> options = availableOptions(gameData, opponentId, controllerId, lifeLoss,
                    discardCount, sacrificePredicate);
            if (options.size() == 1) {
                state.lifeLossCounts.merge(opponentId, 1, Integer::sum);
                continue;
            }
            gameData.rerunCurrentEffectAfterInteraction = true;
            String prompt = sourceName + " — lose " + lifeLoss
                    + " life unless you sacrifice " + sacrificeDescription(sacrificePredicate)
                    + " or discard " + discardDescription(discardCount) + ".";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    opponentId, null, null,
                    new ChoiceContext.TormentPenaltyChoice(opponentId, sourceName),
                    options, prompt));
            return;
        }
    }

    private void applyDeferredMode(GameData gameData, StackEntry entry, String sourceName, int lifeLoss,
            int discardCount, UUID opponentId, String mode, PermanentPredicate sacrificePredicate) {
        TormentState state = gameData.torment;
        if (sacrificeOption(sacrificePredicate).equals(mode)) {
            List<UUID> sacrificeableIds = sacrificeablePermanentIds(gameData, opponentId,
                    entry.getControllerId(), sacrificePredicate);
            if (!sacrificeableIds.isEmpty()) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TormentSacrifice(opponentId));
                playerInputService.beginPermanentChoice(gameData, opponentId, sacrificeableIds,
                        sourceName + " — choose " + sacrificeDescription(sacrificePredicate) + " to sacrifice.");
                return;
            }
        } else if (discardOption(discardCount).equals(mode)) {
            List<Integer> validIndices = unchosenHandIndices(gameData, opponentId);
            if (validIndices.size() >= discardCount) {
                gameData.discardCausedByOpponent = !opponentId.equals(entry.getControllerId());
                gameData.rerunCurrentEffectAfterInteraction = true;
                gameData.deferredDiscardChooserId = opponentId;
                playerInteractionSupport.resolveDiscardCards(gameData, opponentId, discardCount, validIndices);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
                gameData.deferredDiscardChooserId = null;
            }
        } else {
            state.lifeLossCounts.merge(opponentId, 1, Integer::sum);
        }
        advanceDeferred(gameData, entry, sourceName, lifeLoss, discardCount, sacrificePredicate);
    }

    /** Positions in the player's hand of the cards they haven't already chosen to discard. */
    private List<Integer> unchosenHandIndices(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            UUID cardId = hand.get(i).getId();
            boolean chosen = gameData.deferredDiscardSelections.stream()
                    .anyMatch(selection -> selection.cardId().equals(cardId));
            if (!chosen) {
                indices.add(i);
            }
        }
        return indices;
    }

    private void finishDeferred(GameData gameData, StackEntry entry, String sourceName, int lifeLoss,
            int discardCount, PermanentPredicate sacrificePredicate) {
        TormentState state = gameData.torment;
        boolean nextIteration = state.deferEachIteration && state.remainingIterations > 0;
        int iterationsLeft = state.remainingIterations;
        boolean targeted = state.targetedFlow;
        List<UUID> sacrifices = List.copyOf(state.chosenSacrifices);
        Map<UUID, Integer> lifeLosses = new LinkedHashMap<>(state.lifeLossCounts);
        gameData.deferredDiscardChooserId = null;
        gameData.rerunCurrentEffectAfterInteraction = false;
        state.reset();
        for (UUID permanentId : sacrifices) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                destructionSupport.sacrificeAndLog(gameData, permanent,
                        gameQueryService.findPermanentController(gameData, permanentId));
            }
        }
        cardChoiceHandlerService.discardDeferredSelections(gameData, entry.getControllerId());
        lifeLosses.forEach((playerId, times) -> {
            for (int i = 0; i < times; i++) {
                lifeSupport.applyLifeLoss(gameData, playerId, lifeLoss, sourceName);
            }
        });
        if (nextIteration) {
            state.active = true;
            state.deferred = true;
            state.deferEachIteration = true;
            state.targetedFlow = targeted;
            state.totalPasses = 1;
            state.remainingIterations = iterationsLeft - 1;
            state.remaining.addAll(targeted
                    ? targetedPlayer(gameData, entry.getTargetId())
                    : apnapOpponents(gameData, entry.getControllerId()));
            advanceDeferred(gameData, entry, sourceName, lifeLoss, discardCount, sacrificePredicate);
        }
    }

    /**
     * Applies the current opponent's chosen penalty. Sacrifice/discard begin a sub-choice and pause
     * (advancing on completion via the re-run); losing life applies immediately and continues.
     */
    private void applyMode(GameData gameData, StackEntry entry, String sourceName, int lifeLoss,
            int discardCount, UUID opponentId, String mode, PermanentPredicate sacrificePredicate,
            boolean targeted) {
        if (sacrificeOption(sacrificePredicate).equals(mode)) {
            List<UUID> sacrificeableIds = sacrificeablePermanentIds(gameData, opponentId,
                    entry.getControllerId(), sacrificePredicate);
            if (sacrificeableIds.isEmpty()) {
                advance(gameData, entry, sourceName, lifeLoss, discardCount,
                        sacrificePredicate, targeted);
                return;
            }
            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TormentSacrifice(opponentId));
            playerInputService.beginPermanentChoice(gameData, opponentId, sacrificeableIds,
                    sourceName + " — choose " + sacrificeDescription(sacrificePredicate) + " to sacrifice.");
            return;
        }

        if (discardOption(discardCount).equals(mode)) {
            List<Card> hand = gameData.playerHands.get(opponentId);
            if (hand == null || hand.size() < discardCount) {
                lifeSupport.applyLifeLoss(gameData, opponentId, lifeLoss, sourceName);
                advance(gameData, entry, sourceName, lifeLoss, discardCount,
                        sacrificePredicate, targeted);
                return;
            }
            gameData.discardCausedByOpponent = !opponentId.equals(entry.getControllerId());
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, opponentId, discardCount,
                    DiscardFollowUp.NONE);
            if (!gameData.interaction.isAwaitingInput()) {
                // Defensive: the hand emptied out from under us — just continue.
                lifeSupport.applyLifeLoss(gameData, opponentId, lifeLoss, sourceName);
                advance(gameData, entry, sourceName, lifeLoss, discardCount,
                        sacrificePredicate, targeted);
            }
            return;
        }

        // "Lose N life": the player declined to sacrifice or discard.
        lifeSupport.applyLifeLoss(gameData, opponentId, lifeLoss, sourceName);
        advance(gameData, entry, sourceName, lifeLoss, discardCount,
                sacrificePredicate, targeted);
    }

    private List<String> availableOptions(GameData gameData, UUID opponentId, UUID sourceControllerId,
            int lifeLoss, int discardCount, PermanentPredicate sacrificePredicate) {
        List<String> options = new ArrayList<>();
        if (!sacrificeablePermanentIds(gameData, opponentId, sourceControllerId, sacrificePredicate).isEmpty()) {
            options.add(sacrificeOption(sacrificePredicate));
        }
        List<Card> hand = gameData.playerHands.get(opponentId);
        int availableCards = gameData.torment.deferred
                ? unchosenHandIndices(gameData, opponentId).size()
                : hand == null ? 0 : hand.size();
        if (availableCards >= discardCount) {
            options.add(discardOption(discardCount));
        }
        options.add("Lose " + lifeLoss + " life");
        return options;
    }

    /** Permanents the opponent may sacrifice to this penalty; none if an opponent's effect can't force a sacrifice. */
    private List<UUID> sacrificeablePermanentIds(GameData gameData, UUID opponentId, UUID sourceControllerId,
            PermanentPredicate sacrificePredicate) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, opponentId, sourceControllerId)) {
            return List.of();
        }
        return destructionSupport.collectPermanentIds(gameData, opponentId,
                p -> !gameData.torment.chosenSacrifices.contains(p.getId()) && (sacrificePredicate == null
                        ? !p.getCard().hasType(CardType.LAND)
                        : predicateEvaluationService.matchesPermanentPredicate(
                                gameData, p, sacrificePredicate)));
    }

    private String sacrificeDescription(PermanentPredicate sacrificePredicate) {
        if (sacrificePredicate instanceof PermanentIsCreaturePredicate) {
            return "a creature";
        }
        if (sacrificePredicate instanceof PermanentAnyOfPredicate anyOf
                && anyOf.predicates().stream().anyMatch(PermanentIsCreaturePredicate.class::isInstance)
                && anyOf.predicates().stream().anyMatch(PermanentIsPlaneswalkerPredicate.class::isInstance)) {
            return "a creature or planeswalker";
        }
        return "a nonland permanent";
    }

    private String sacrificeOption(PermanentPredicate sacrificePredicate) {
        return sacrificePredicate instanceof PermanentAnyOfPredicate
                ? ChoiceContext.TormentPenaltyChoice.sacrifice(sacrificeDescription(sacrificePredicate))
                : ChoiceContext.TormentPenaltyChoice.SACRIFICE;
    }

    private String discardOption(int discardCount) {
        return ChoiceContext.TormentPenaltyChoice.discard(discardCount);
    }

    private String discardDescription(int discardCount) {
        return discardCount == 1 ? "a card" : discardCount + " cards";
    }

    private List<UUID> targetedPlayer(GameData gameData, UUID targetId) {
        return targetId != null && gameData.playerIds.contains(targetId) ? List.of(targetId) : List.of();
    }

    /** Opponents of {@code controllerId} in APNAP order (active player first). */
    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex > 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        List<UUID> opponents = new ArrayList<>();
        for (UUID id : rotated) {
            if (!id.equals(controllerId)) {
                opponents.add(id);
            }
        }
        return opponents;
    }
}
