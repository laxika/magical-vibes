package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeForColorlessManaUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MayPayLifeForColorlessManaUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLifeForColorlessManaUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.mayPayLifeForColorlessManaUntilEndOfTurn.add(controllerId);

        String logEntry = entry.getCard().getName()
                + ": until end of turn, you may pay 1 life to add {C} any time you could activate a mana ability.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), ": until end of turn, you may pay 1 life to add {C} any time you could activate a mana ability."));
        log.info("Game {} - {} may pay life for colorless mana until end of turn", gameData.id, controllerId);
    }
}
