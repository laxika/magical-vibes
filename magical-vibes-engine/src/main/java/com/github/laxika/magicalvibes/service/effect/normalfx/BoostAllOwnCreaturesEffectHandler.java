package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostAllOwnCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostAllOwnCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostAllOwnCreaturesEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());

        // Lock the amount in once, before any boost lands — re-reading per creature would inflate
        // power-derived amounts (e.g. Overwhelming Stampede) as earlier boosts raise the max power.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        AmountContext ctx = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx);

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (boost.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(permanent, boost.filter(), filterContext))) {
                permanent.setPowerModifier(permanent.getPowerModifier() + powerBoost);
                permanent.setToughnessModifier(permanent.getToughnessModifier() + toughnessBoost);
                count++;
            }
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(String.format(" gives %+d/%+d to %d creature(s) until end of turn.",
                        powerBoost, toughnessBoost, count))
                .build());

        log.info("Game {} - {} boosts {} creatures {}/{}", gameData.id, entry.getCard().getName(), count, powerBoost, toughnessBoost);
    }
}
