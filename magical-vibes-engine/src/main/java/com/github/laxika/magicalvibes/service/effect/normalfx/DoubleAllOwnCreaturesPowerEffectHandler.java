package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleAllOwnCreaturesPowerEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoubleAllOwnCreaturesPowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleAllOwnCreaturesPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var doubling = (DoubleAllOwnCreaturesPowerEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : entry.getSourcePermanentSnapshot();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(source);
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || (doubling.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                                permanent, doubling.filter(), filterContext))) {
                continue;
            }
            int currentPower = gameQueryService.getEffectivePower(gameData, permanent);
            permanent.setPowerModifier(permanent.getPowerModifier()
                    + MaroGoneNutsSupport.apply(gameData, effect, currentPower));
            count++;
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" doubles the power of " + count + " creature(s) until end of turn.")
                .build());

        log.info("Game {} - {} doubles the power of {} creatures", gameData.id,
                entry.getCard().getName(), count);
    }
}
