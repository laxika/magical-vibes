package com.github.laxika.magicalvibes.service.effect.normalfx;

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
        gameData.extraManaOnLandSubtypeTapThisTurn.put(e.subtype(), e.color());

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": until end of turn, tapping a " + e.subtype().name().toLowerCase()
                        + " for mana adds an additional " + e.color().getCode() + "."));
        log.info("Game {} - {} sets extra {} mana on {} taps this turn", gameData.id,
                entry.getCard().getName(), e.color(), e.subtype());
    }
}
