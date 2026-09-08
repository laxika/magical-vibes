package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            advance(gameData, entry, sourceName, lifeLoss, discardCount,
                    torment.sacrificePredicate(), torment.targeted());
            return;
        }

        if (state.chosenMode != null) {
            // The current opponent just picked a penalty option — apply it.
            String mode = state.chosenMode;
            state.chosenMode = null;
            applyMode(gameData, entry, sourceName, lifeLoss, discardCount,
                    state.currentOpponentId, mode, torment.sacrificePredicate(), torment.targeted());
            return;
        }

        // Re-entry after a discard / sacrifice sub-choice completed — advance to the next opponent.
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

            List<String> options = availableOptions(gameData, opponentId, lifeLoss, discardCount,
                    sacrificePredicate);
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
     * Applies the current opponent's chosen penalty. Sacrifice/discard begin a sub-choice and pause
     * (advancing on completion via the re-run); losing life applies immediately and continues.
     */
    private void applyMode(GameData gameData, StackEntry entry, String sourceName, int lifeLoss,
            int discardCount, UUID opponentId, String mode, PermanentPredicate sacrificePredicate,
            boolean targeted) {
        if (sacrificeOption(sacrificePredicate).equals(mode)) {
            List<UUID> sacrificeableIds = sacrificeablePermanentIds(gameData, opponentId, sacrificePredicate);
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

    private List<String> availableOptions(GameData gameData, UUID opponentId, int lifeLoss,
            int discardCount, PermanentPredicate sacrificePredicate) {
        List<String> options = new ArrayList<>();
        if (!sacrificeablePermanentIds(gameData, opponentId, sacrificePredicate).isEmpty()) {
            options.add(sacrificeOption(sacrificePredicate));
        }
        List<Card> hand = gameData.playerHands.get(opponentId);
        if (hand != null && hand.size() >= discardCount) {
            options.add(discardOption(discardCount));
        }
        options.add("Lose " + lifeLoss + " life");
        return options;
    }

    private List<UUID> sacrificeablePermanentIds(GameData gameData, UUID opponentId,
            PermanentPredicate sacrificePredicate) {
        return destructionSupport.collectPermanentIds(gameData, opponentId,
                p -> sacrificePredicate == null
                        ? !p.getCard().hasType(CardType.LAND)
                        : predicateEvaluationService.matchesPermanentPredicate(
                                gameData, p, sacrificePredicate));
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
