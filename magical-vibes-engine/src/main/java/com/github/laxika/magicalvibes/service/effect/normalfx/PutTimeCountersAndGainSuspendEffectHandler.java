package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersAndGainSuspendEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PutTimeCountersAndGainSuspendEffect}: each card its subject names in exile gets the time counters and
 * is suspended. It never prompts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTimeCountersAndGainSuspendEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTimeCountersAndGainSuspendEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var suspend = (PutTimeCountersAndGainSuspendEffect) effect;
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, suspend, suspend.cards())) {
            Card card = located.card();
            if (located.zone() != Zone.EXILE || card.isToken()) {
                continue;
            }
            int counters = suspend.equalToEachManaValue() ? card.getManaValue() : suspend.timeCounters();
            if (counters < 1) {
                continue;
            }
            gameData.setExiledCardCounters(card.getId(), com.github.laxika.magicalvibes.model.CounterType.TIME, counters);
            gameLogService.append(gameData, GameLog.cardThenUnlessHidden(gameData, card,
                    " gets " + counters + " time counters and gains suspend."));
            log.info("Game {} - {} gets {} time counters and gains suspend",
                    gameData.id, card.getName(), counters);
        }
    }
}
