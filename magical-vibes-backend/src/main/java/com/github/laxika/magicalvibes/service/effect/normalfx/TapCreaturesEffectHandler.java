package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var tap = (TapCreaturesEffect) effect;
        gameData.forEachPermanent((playerId, p) -> {
            if (!gameQueryService.isCreature(gameData, p)) return;
            if (!gameQueryService.matchesFilters(
                    p,
                    tap.filters(),
                    FilterContext.of(gameData)
                            .withSourceCardId(entry.getCard().getId())
                            .withSourceControllerId(entry.getControllerId()))) return;

            tapUntapSupport.tapPermanent(gameData, p);

            String logMsg = entry.getCard().getName() + " taps " + p.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
        });

        log.info("Game {} - {} taps creatures matching filters", gameData.id, entry.getCard().getName());
    }
}
