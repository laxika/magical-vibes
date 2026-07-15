package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
    private final GameBroadcastService gameBroadcastService;
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
                .withSourceControllerId(entry.getControllerId());
        final int[] count = {0};

        java.util.function.BiConsumer<UUID, Permanent> apply = (playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (boost.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(permanent, boost.filter(), filterContext))) {
                permanent.setPowerModifier(permanent.getPowerModifier() + powerBoost);
                permanent.setToughnessModifier(permanent.getToughnessModifier() + toughnessBoost);
                count[0]++;
            }
        };

        if (boost.scope() == EachPermanentScope.TARGET_PLAYER) {
            UUID targetPlayerId = entry.getTargetId();
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

        String logEntry = String.format("%s gives %+d/%+d to %d creature(s) until end of turn.",
                entry.getCard().getName(), powerBoost, toughnessBoost, count[0]);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gives {}/{} to {} creatures", gameData.id, entry.getCard().getName(), powerBoost, toughnessBoost, count[0]);
    }
}
