package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

@Slf4j
@Component
@RequiredArgsConstructor
public class CantBlockThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantBlockThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CantBlockThisTurnEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(entry.getControllerId());
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry, e);
            case ENCHANTED -> resolveEnchanted(gameData, entry);
            case TARGET_PLAYERS_PERMANENTS -> resolveTargetPlayersPermanents(gameData, entry, e, filterContext);
            case TARGET_CONTROLLERS_OTHER_CREATURES -> resolveTargetControllersOtherCreatures(gameData, entry, e);
            case ALL_CREATURES -> resolveAllCreatures(gameData, entry, e);
            default -> throw new IllegalStateException("Unsupported can't-block scope: " + e.scope());
        }
    }

    private void resolveTarget(GameData gameData, StackEntry entry, CantBlockThisTurnEffect effect) {
        // Multi-target: apply to each valid target of the group this effect is bound to
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            for (UUID targetId : entry.targetsForEffect(effect)) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue;
                }
                resolveForTarget(gameData, target);
            }
            return;
        }

        // Single-target fallback: a bound effect whose group was omitted has no target
        if (entry.targetsForEffect(effect).isEmpty() && entry.getTargetId() != null
                && entry.getTargetingCard() != null && entry.getTargetingCard().hasEffectTargetIndex(effect)) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        resolveForTarget(gameData, target);
    }

    void resolveForTarget(GameData gameData, Permanent target) {
        target.setCantBlockThisTurn(true);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't block this turn."));
        log.info("Game {} - {} can't block this turn", gameData.id, target.getCard().getName());
    }

    private void resolveEnchanted(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent enchanted;
        if (source != null && source.isAttached()) {
            enchanted = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        } else {
            // The Aura left the battlefield in response: the ability still affects the permanent it
            // was attached to when it was activated (captured as the entry's target).
            enchanted = entry.getTargetId() == null
                    ? null : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        }
        if (enchanted == null) {
            return;
        }

        enchanted.setCantBlockThisTurn(true);
        gameLogService.append(gameData, GameLog.cardThen(enchanted.getCard(), " can't block this turn."));
        log.info("Game {} - {} can't block this turn", gameData.id, enchanted.getCard().getName());
    }

    private void resolveTargetPlayersPermanents(GameData gameData, StackEntry entry, CantBlockThisTurnEffect e,
                                                FilterContext filterContext) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        // Determine the affected player: if target is a player, use directly;
        // if target is a planeswalker, use its controller
        UUID affectedPlayerId;
        if (gameData.playerIds.contains(targetId)) {
            affectedPlayerId = targetId;
        } else {
            affectedPlayerId = gameQueryService.findPermanentController(gameData, targetId);
            if (affectedPlayerId == null) return;
        }

        var controlled = new PermanentControlledByPlayerPredicate(affectedPlayerId);
        var predicate = e.filter() == null ? controlled
                : new PermanentAllOfPredicate(List.of(controlled, e.filter()));
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(),
                new MatchingCreaturesCantBlockMatchingCreaturesEffect(predicate,
                        new PermanentTruePredicate(), "can't block this turn"),
                null, null, predicate, EffectDuration.UNTIL_END_OF_TURN, 0));
        gameLogService.append(gameData, GameLog.text("Creatures controlled by "
                + gameData.playerIdToName.get(affectedPlayerId) + " can't block this turn."));
    }

    /**
     * "Other creatures that player controls can't block this turn" — the affected player is the
     * target permanent's controller, and the target itself is deliberately left able to block.
     * The restriction does not modify characteristics (CR 611.2c), so it is a floating rule that
     * also covers creatures that enter under that player's control later in the turn.
     */
    private void resolveTargetControllersOtherCreatures(GameData gameData, StackEntry entry, CantBlockThisTurnEffect e) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        UUID affectedPlayerId = gameQueryService.findPermanentController(gameData, targetId);
        if (affectedPlayerId == null) return;

        List<PermanentPredicate> conditions = new ArrayList<>(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByPlayerPredicate(affectedPlayerId),
                new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(targetId))));
        if (e.filter() != null) {
            conditions.add(e.filter());
        }
        PermanentPredicate predicate = new PermanentAllOfPredicate(conditions);

        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(),
                new MatchingCreaturesCantBlockMatchingCreaturesEffect(predicate,
                        new PermanentTruePredicate(), "can't block this turn"),
                null, null, predicate, EffectDuration.UNTIL_END_OF_TURN, 0));

        String playerName = gameData.playerIdToName.get(affectedPlayerId);
        gameLogService.append(gameData, GameLog.text(
                "Other creatures controlled by " + playerName + " can't block this turn."));
        log.info("Game {} - other creatures controlled by {} can't block this turn", gameData.id, playerName);
    }

    private void resolveAllCreatures(GameData gameData, StackEntry entry, CantBlockThisTurnEffect effect) {
        var predicate = effect.filter() == null ? new PermanentTruePredicate() : effect.filter();
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(),
                new MatchingCreaturesCantBlockMatchingCreaturesEffect(predicate,
                        new PermanentTruePredicate(), "can't block this turn"),
                null, null, predicate, EffectDuration.UNTIL_END_OF_TURN, 0));
        gameLogService.append(gameData, GameLog.text("Matching creatures can't block this turn."));
    }
}
