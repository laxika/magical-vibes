package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class LandsOfSubtypeAddExtraManaUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect) effect;
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), effect, null, Duration.UNTIL_END_OF_TURN));

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": until end of turn, tapping a " + e.subtype().name().toLowerCase()
                        + " for mana adds an additional " + e.color().getCode() + "."));
        log.info("Game {} - {} sets extra {} mana on {} taps this turn", gameData.id,
                entry.getCard().getName(), e.color(), e.subtype());
    }
}
