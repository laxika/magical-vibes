package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageExceptBySubtypesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventCombatDamageExceptBySubtypesEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventCombatDamageExceptBySubtypesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventCombatDamageExceptBySubtypesEffect) effect;
        gameData.combatDamageExemptPredicate = e.exemptPredicate();

        String logEntry = "Combat damage from creatures that don't match the exemption will be prevented this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }
}
