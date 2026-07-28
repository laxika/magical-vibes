package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPlayersPermanentsAndDamageEqualToCountEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapPlayersPermanentsAndDamageEqualToCountEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapPlayersPermanentsAndDamageEqualToCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TapPlayersPermanentsAndDamageEqualToCountEffect) effect;

        UUID playerId = entry.getTargetId();
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        int tapped = 0;
        for (Permanent permanent : List.copyOf(battlefield)) {
            if (permanent.isTapped()) continue;
            if (!predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) continue;

            if (tapUntapSupport.tapPermanent(gameData, permanent)) {
                tapped++;
            }
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" taps " + tapped + " permanent(s).")
                .build());
        log.info("Game {} - {} taps {} permanent(s) of the end-step player", gameData.id, entry.getCard().getName(), tapped);

        if (tapped > 0 && !damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int damage = gameQueryService.applyDamageMultiplier(gameData, tapped, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
