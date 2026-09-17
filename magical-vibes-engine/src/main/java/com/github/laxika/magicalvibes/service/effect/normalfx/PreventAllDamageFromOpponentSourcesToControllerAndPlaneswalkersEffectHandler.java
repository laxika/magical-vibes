package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ComeuppanceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageFromOpponentSourcesToControllerAndPlaneswalkersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Comeuppance's turn-long source-sensitive damage prevention. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreventAllDamageFromOpponentSourcesToControllerAndPlaneswalkersEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventAllDamageFromOpponentSourcesToControllerAndPlaneswalkersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.comeuppanceDamagePreventionShields.add(
                new ComeuppanceDamagePreventionShield(entry.getControllerId(), entry.getCard()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " prevents all damage from sources its controller's opponents control to that player "
                        + "and planeswalkers they control this turn."));
        log.info("Game {} - Comeuppance shield added for {}", gameData.id,
                gameData.playerIdToName.get(entry.getControllerId()));
    }
}
