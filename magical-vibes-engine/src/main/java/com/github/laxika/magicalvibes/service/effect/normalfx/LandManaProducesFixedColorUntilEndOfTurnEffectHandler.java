package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LandManaProducesFixedColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LandManaProducesFixedColorUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LandManaProducesFixedColorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var fixedColorEffect = (LandManaProducesFixedColorUntilEndOfTurnEffect) effect;
        gameData.landManaFixedColorThisTurn.put(entry.getControllerId(), fixedColorEffect.color());

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": until end of turn, your lands tapped for mana produce "
                        + fixedColorEffect.color().getCode() + " instead of any other type."));
        log.info("Game {} - {} makes their lands produce {} this turn", gameData.id,
                entry.getCard().getName(), fixedColorEffect.color());
    }
}
