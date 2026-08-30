package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a continuation by placing its effect on the stack as a reflexive ability. */
@Component
@RequiredArgsConstructor
public class QueueReflexiveAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return QueueReflexiveAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        QueueReflexiveAbilityEffect queueEffect = (QueueReflexiveAbilityEffect) effect;
        if (graveyardTargetingSupport.findTarget(List.of(queueEffect.effect())) != null
                || queueEffect.effect().targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    entry.getCard(), entry.getControllerId(), List.of(queueEffect.effect())));
            return;
        }
        if (beginPermanentOrPlayerTargetChoice(gameData, entry, queueEffect.effect(), queueEffect.optionalTarget())) {
            return;
        }
        StackEntry reflexiveAbility = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s reflexive ability",
                new ArrayList<>(List.of(queueEffect.effect())),
                0,
                entry.getSourcePermanentId());
        reflexiveAbility.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        gameData.stack.add(reflexiveAbility);
    }

    private boolean beginPermanentOrPlayerTargetChoice(GameData gameData, StackEntry entry, CardEffect effect,
                                                       boolean optionalTarget) {
        TargetSpec targetSpec = effect.targetSpec();
        if (!targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                && !targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            return false;
        }

        TargetPredicate predicate = targetSpec.targetPredicate();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withXValue(entry.getXValue());

        List<UUID> validPermanentIds = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                    if (targetPredicateEvaluationService.matchesPermanent(predicate, permanent, filterContext)) {
                        validPermanentIds.add(permanent.getId());
                    }
                }
            }
        }

        List<UUID> validPlayerIds = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (targetPredicateEvaluationService.matchesPlayer(
                        predicate, playerId, entry.getControllerId(), gameData)) {
                    validPlayerIds.add(playerId);
                }
            }
        }
        if (optionalTarget && !validPlayerIds.contains(entry.getControllerId())) {
            validPlayerIds.add(entry.getControllerId());
        }

        if (validPermanentIds.isEmpty() && validPlayerIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s reflexive ability has no valid targets."));
            return true;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), List.of(effect), entry.getSourcePermanentId(),
                entry.getSourcePermanentSnapshot(), entry.getEventValue(), entry.getXValue(),
                optionalTarget));
        playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), validPermanentIds,
                validPlayerIds, entry.getCard().getName() + "'s reflexive ability - Choose a target.");
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                "'s reflexive ability - choose a target."));
        return true;
    }
}
