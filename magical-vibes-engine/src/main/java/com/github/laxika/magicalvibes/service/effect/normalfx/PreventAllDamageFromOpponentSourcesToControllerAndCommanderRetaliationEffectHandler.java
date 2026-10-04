package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.JudgmentOfAlexanderDamagePreventionShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageFromOpponentSourcesToControllerAndCommanderRetaliationEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Judgment of Alexander's turn-long prevention shield. */
@Component
@RequiredArgsConstructor
public class PreventAllDamageFromOpponentSourcesToControllerAndCommanderRetaliationEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventAllDamageFromOpponentSourcesToControllerAndCommanderRetaliationEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.judgmentOfAlexanderDamagePreventionShields.add(
                new JudgmentOfAlexanderDamagePreventionShield(entry.getControllerId(), entry.getCard()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " prevents all damage from sources its controller's opponents control to that player this turn."));
    }
}
