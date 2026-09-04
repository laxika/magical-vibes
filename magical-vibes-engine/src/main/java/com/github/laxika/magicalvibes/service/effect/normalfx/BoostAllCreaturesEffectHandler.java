package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostAllCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostAllCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostAllCreaturesEffect) effect;

        // Lock the amount in once, before any boost lands, then apply it uniformly.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        AmountContext ctx = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx);

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId());
        final int[] count = {0};

        java.util.function.BiConsumer<UUID, Permanent> apply = (playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (boost.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(permanent, boost.filter(), filterContext))) {
                applyBoost(gameData, entry, permanent, powerBoost, toughnessBoost, boost.duration());
                count[0]++;
            }
        };

        if (boost.scope() == EachPermanentScope.TARGET_PLAYER) {
            UUID targetPlayerId = entry.getTargetId();
            if (targetPlayerId == null) {
                List<UUID> targets = entry.targetsForEffect(boost);
                targetPlayerId = targets.isEmpty() ? null : targets.getFirst();
            }
            if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                return;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            if (battlefield != null) {
                for (Permanent permanent : new java.util.ArrayList<>(battlefield)) {
                    apply.accept(targetPlayerId, permanent);
                }
            }
        } else {
            gameData.forEachPermanent(apply);
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(String.format(" gives %+d/%+d to %d creature(s) %s.",
                        powerBoost, toughnessBoost, count[0],
                        boost.duration() == GrantDuration.UNTIL_YOUR_NEXT_TURN
                                ? "until your next turn" : "until end of turn"))
                .build());

        log.info("Game {} - {} gives {}/{} to {} creatures", gameData.id, entry.getCard().getName(), powerBoost, toughnessBoost, count[0]);
    }

    private void applyBoost(GameData gameData, StackEntry entry, Permanent target,
                            int powerBoost, int toughnessBoost, GrantDuration duration) {
        if (duration == GrantDuration.UNTIL_YOUR_NEXT_TURN) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(),
                    new BuffTargetCreatureIndefinitelyEffect(powerBoost, toughnessBoost),
                    target.getId(), null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
        } else {
            target.setPowerModifier(target.getPowerModifier() + powerBoost);
            target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);
        }
    }
}
