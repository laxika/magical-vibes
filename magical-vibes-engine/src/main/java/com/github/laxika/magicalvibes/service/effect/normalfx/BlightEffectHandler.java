package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the blight action after its optional may choice has been accepted. */
@Component
@RequiredArgsConstructor
public class BlightEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BlightEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BlightEffect blight = (BlightEffect) effect;
        resolveForPlayer(gameData, entry, blight, entry.getControllerId());
    }

    public void resolveForPlayer(GameData gameData, StackEntry entry, BlightEffect blight,
                                 UUID controllerId) {
        List<UUID> creatureIds = controlledCreatureIds(gameData, controllerId);

        if (creatureIds.isEmpty()) {
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                placeCountersAndQueueThen(gameData, entry, creature, blight);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BlightCreatureChoice(
                controllerId, entry.getCard(), entry.getSourcePermanentId(), blight));
        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                entry.getCard().getName() + " - Choose a creature to blight.");
    }

    public void placeCountersAndQueueThen(GameData gameData, StackEntry entry, Permanent creature,
                                          BlightEffect blight) {
        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, creature, CounterType.MINUS_ONE_MINUS_ONE, blight.count());
        if (blight.thenEffect() == null) {
            return;
        }

        if (blight.thenEffectTargets()) {
            queueTargetedReflexiveAbility(gameData, entry, blight.thenEffect());
            return;
        }

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s reflexive ability",
                new ArrayList<>(List.of(blight.thenEffect())),
                creature.getId(),
                entry.getSourcePermanentId()));
    }

    private void queueTargetedReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        TargetSpec targetSpec = thenEffect.targetSpec();
        TargetPredicate targetPredicate = targetSpec.targetPredicate();
        List<UUID> validPermanentTargets = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard().getId())
                    .withSourceControllerId(entry.getControllerId());
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent permanent : battlefield) {
                    if (targetPredicateEvaluationService.matchesPermanent(targetPredicate, permanent, filterContext)) {
                        validPermanentTargets.add(permanent.getId());
                    }
                }
            }
        }

        List<UUID> validPlayerTargets = targetSpec.admits(TargetPredicate.Kind.PLAYER)
                ? gameData.orderedPlayerIds.stream()
                        .filter(playerId -> targetPredicateEvaluationService.matchesPlayer(
                                targetPredicate, playerId, entry.getControllerId(), gameData))
                        .toList()
                : List.of();
        if (validPermanentTargets.isEmpty() && validPlayerTargets.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), List.of(thenEffect)));
        if (validPlayerTargets.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validPermanentTargets,
                    entry.getCard().getName() + "'s reflexive ability - Choose target.");
        } else {
            playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), validPermanentTargets,
                    validPlayerTargets, entry.getCard().getName() + "'s reflexive ability - Choose target.");
        }
    }

    private List<UUID> controlledCreatureIds(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }
}
