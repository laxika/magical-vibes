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
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;

        var prevention = (PreventAllCombatDamageToControllerAndCreateTokensEffect) effect;
        gameData.combatDamagePreventionTokenShields.add(new CombatDamagePreventionTokenShield(
                controllerId, prevention.token(), controllerId, entry.getCard().getSetCode()));
        gameLogService.append(gameData, GameLog.text(
                "All combat damage that would be dealt to "
                        + gameData.playerIdToName.get(controllerId)
                        + " this turn will be prevented; create one token for each damage prevented."));
    }
}
