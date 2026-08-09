package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LandManaProducesOneChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LandManaProducesOneChosenColorUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LandManaProducesOneChosenColorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.playersWithLandManaChoiceReplacementThisTurn.add(entry.getControllerId());

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": until end of turn, lands tapped for mana produce one mana of a color of your choice."));
        log.info("Game {} - {} makes their land taps produce one chosen-color mana this turn",
                gameData.id, entry.getCard().getName());
    }
}
