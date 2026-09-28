package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CombatDamagePreventionTokenShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToControllerAndCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Inkshield's combat-damage prevention and token rider. */
@Component
@RequiredArgsConstructor
public class PreventAllCombatDamageToControllerAndCreateTokensEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventAllCombatDamageToControllerAndCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getControllerId() == null) {
            return;
        }
        var e = (PreventAllCombatDamageToControllerAndCreateTokensEffect) effect;
        gameData.combatDamagePreventionTokenShields.putIfAbsent(entry.getControllerId(),
                new CombatDamagePreventionTokenShield(e.token(), entry.getCard().getSetCode()));
        gameLogService.append(gameData, GameLog.text(
                "All combat damage that would be dealt to you this turn is prevented. "
                        + "Create one token for each damage prevented."));
    }
}
