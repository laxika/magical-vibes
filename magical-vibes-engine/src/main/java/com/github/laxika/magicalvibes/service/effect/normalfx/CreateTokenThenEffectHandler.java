package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenThenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenThenEffect createThen = (CreateTokenThenEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();

        createTokenEffectHandler.resolve(gameData, entry, createThen.tokenEffect());
        if (gameData.resolvingMayEffectFromStack || !gameData.pendingMayAbilities.isEmpty()) {
            return;
        }
        if (entry.getCreatedPermanentIds().size() == createdBefore) {
            return;
        }

        queueTargetedReflexiveAbility(gameData, entry, createThen.thenEffect());
    }

    void queueTargetedReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        queueTargetedReflexiveAbility(gameData, entry, thenEffect, null, false);
    }

    void queueTargetedReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect,
                                       UUID sourcePermanentId, boolean optionalTarget) {
        if (graveyardTargetingSupport.findTarget(List.of(thenEffect)) != null) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    entry.getCard(), entry.getControllerId(), List.of(thenEffect)));
            return;
        }

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
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new))
                : new ArrayList<>();
        if (validPermanentTargets.isEmpty() && validPlayerTargets.isEmpty()) {
            if (optionalTarget) {
                gameData.stack.add(new StackEntry(
                        com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY,
                        entry.getCard(), entry.getControllerId(),
                        entry.getCard().getName() + "'s reflexive ability",
                        List.of(thenEffect), (UUID) null, sourcePermanentId));
            }
            return;
        }

        if (optionalTarget) {
            validPlayerTargets.add(entry.getControllerId());
        }
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), List.of(thenEffect),
                sourcePermanentId, null, 0, 0, optionalTarget));
        if (validPlayerTargets.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validPermanentTargets,
                    entry.getCard().getName() + "'s reflexive ability - Choose target.");
        } else {
            playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), validPermanentTargets,
                    validPlayerTargets, optionalTarget
                            ? entry.getCard().getName() + "'s reflexive ability - Choose up to one target (choose yourself to decline)."
                            : entry.getCard().getName() + "'s reflexive ability - Choose target.");
        }
    }
}
