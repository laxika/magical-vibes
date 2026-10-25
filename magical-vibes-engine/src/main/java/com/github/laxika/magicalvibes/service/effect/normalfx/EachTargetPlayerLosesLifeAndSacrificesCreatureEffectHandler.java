package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerLosesLifeAndSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * "Each target player loses life and sacrifices a creature", in either order. Marked {@link AsksDeathReplacementsFirst}:
 * the losing-first form's first change is the losses, and the sacrificing-first form changes nothing before its
 * sacrifice step; its losses follow as a {@link PlayersLoseLifeEffect} step.
 */
@Component
@RequiredArgsConstructor
public class EachTargetPlayerLosesLifeAndSacrificesCreatureEffectHandler
        implements NormalEffectHandlerBean, ContinuesAfterDeparture, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachTargetPlayerLosesLifeAndSacrificesCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachTargetPlayerLosesLifeAndSacrificesCreatureEffect loseLifeAndSacrifice =
                (EachTargetPlayerLosesLifeAndSacrificesCreatureEffect) effect;
        List<UUID> allTargetPlayerIds = targetPlayerIds(entry);
        if (allTargetPlayerIds.isEmpty()) {
            beginNextTarget(gameData, entry, entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(),
                    List.of(), List.of(), List.of(), loseLifeAndSacrifice);
            return;
        }

        if (!loseLifeAndSacrifice.sacrificeBeforeLifeLoss()) {
            // The losses are this step's first change, so every Lich's Duel Mastery sacrifice that replaces one
            // of them asks its CR 616.1 questions before any life is lost.
            lifeSupport.applyLifeLosses(gameData, losses(gameData, allTargetPlayerIds, loseLifeAndSacrifice.lifeLoss()),
                    entry.getCard().getName());
        }

        beginNextTarget(gameData, entry, entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(),
                allTargetPlayerIds, apnapTargets(gameData, allTargetPlayerIds), List.of(), loseLifeAndSacrifice);
    }

    private static List<UUID> targetPlayerIds(StackEntry entry) {
        List<UUID> targetPlayerIds = entry.getTargetIds();
        if (targetPlayerIds == null || targetPlayerIds.isEmpty()) {
            return entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        }
        return List.copyOf(targetPlayerIds);
    }

    /**
     * "Then each loses life", for the form that sacrifices first, once the sacrifice step is done: the losses are the
     * next step (CR 608.2c), since the sacrifice can take away a replacement the Lich's Duel Mastery sacrifice that
     * replaces a loss would have, so that sacrifice asks its CR 616.1 questions before any life is lost.
     */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        var loseLifeAndSacrifice = (EachTargetPlayerLosesLifeAndSacrificesCreatureEffect) effect;
        if (loseLifeAndSacrifice.sacrificeBeforeLifeLoss()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(new PlayersLoseLifeEffect(
                    losses(gameData, targetPlayerIds(entry), loseLifeAndSacrifice.lifeLoss()))));
        }
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.EachTargetPlayerLosesLifeAndSacrificesCreature context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null || !context.choosingPlayerId().equals(
                gameQueryService.findPermanentController(gameData, permanentId))
                || !gameQueryService.isCreature(gameData, chosen)
                || gameQueryService.cantBeSacrificed(gameData, chosen)
                || !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, chosen, context.sacrificeFilter())) {
            throw new IllegalStateException("Chosen permanent does not match the sacrifice requirement");
        }

        List<UUID> chosenCreatureIds = new ArrayList<>(context.chosenCreatureIds());
        chosenCreatureIds.add(permanentId);
        beginNextTarget(gameData, null, context.sourceControllerId(), context.sourceCard(), context.sourcePermanentId(),
                context.targetPlayerIds(), context.remainingTargetPlayerIds(), chosenCreatureIds,
                new EachTargetPlayerLosesLifeAndSacrificesCreatureEffect(
                        context.lifeLoss(), context.sacrificeFilter(), context.sacrificeBeforeLifeLoss()));
    }

    /**
     * @param resolvingEntry the entry whose step this is when no choice has been asked yet, {@code null} from a
     *                       choice completion (which already asks before anything leaves, in its replay window)
     */
    private void beginNextTarget(GameData gameData, StackEntry resolvingEntry, UUID sourceControllerId,
                                 Card sourceCard,
                                 UUID sourcePermanentId, List<UUID> targetPlayerIds,
                                 List<UUID> remainingTargetPlayerIds, List<UUID> chosenCreatureIds,
                                 EachTargetPlayerLosesLifeAndSacrificesCreatureEffect effect) {
        List<UUID> remaining = new ArrayList<>(remainingTargetPlayerIds);
        List<UUID> chosen = new ArrayList<>(chosenCreatureIds);

        while (!remaining.isEmpty()) {
            UUID targetPlayerId = remaining.removeFirst();
            if (!gameData.playerIds.contains(targetPlayerId)
                    || !gameQueryService.canEffectCauseSacrifice(gameData, targetPlayerId, sourceControllerId)) {
                continue;
            }

            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, targetPlayerId,
                    permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent)
                            && predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, effect.sacrificeFilter()));
            if (creatureIds.isEmpty()) {
                continue;
            }
            if (creatureIds.size() == 1) {
                chosen.add(creatureIds.getFirst());
                continue;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.EachTargetPlayerLosesLifeAndSacrificesCreature(
                            targetPlayerId, sourceControllerId, sourceCard, sourcePermanentId,
                            List.copyOf(targetPlayerIds), List.copyOf(remaining), List.copyOf(chosen),
                            effect.lifeLoss(), effect.sacrificeFilter(), effect.sacrificeBeforeLifeLoss()));
            playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                    sourceCard.getName() + " - Choose a creature to sacrifice.");
            return;
        }

        if (resolvingEntry != null) {
            // The sacrifice is a step departure: a CR 616.1 question ends the step before any creature leaves, and
            // the life loss of the sacrifice-first form follows once it is done.
            stepDepartureSupport.departThenContinue(gameData, resolvingEntry, effect,
                    DepartureBatch.of(StepDepartureSupport.sacrifices(chosen)), this);
            return;
        }
        destructionSupport.performSimultaneousSacrifice(gameData, chosen);
        if (effect.sacrificeBeforeLifeLoss()) {
            Map<UUID, Integer> losses = losses(gameData, targetPlayerIds, effect.lifeLoss());
            StackEntry parked = gameData.pendingEffectResolutionEntry;
            if (parked != null) {
                // The losses are the step the parked resolution resumes with, as after a sacrifice step.
                parked.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                        List.of(new PlayersLoseLifeEffect(losses)));
            } else {
                lifeSupport.applyLifeLosses(gameData, losses, sourceCard.getName());
            }
        }
    }

    private static Map<UUID, Integer> losses(GameData gameData, List<UUID> targetPlayerIds, int amount) {
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID targetPlayerId : targetPlayerIds) {
            if (gameData.playerIds.contains(targetPlayerId)) {
                losses.put(targetPlayerId, amount);
            }
        }
        return losses;
    }

    private List<UUID> apnapTargets(GameData gameData, List<UUID> targetPlayerIds) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex > 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }

        return rotated.stream()
                .filter(targetPlayerIds::contains)
                .toList();
    }
}
