package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextCombatDamageByTargetCreatureAndCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Ria Ivor's combat-damage prevention shield. */
@Component
@RequiredArgsConstructor
public class PreventNextCombatDamageByTargetCreatureAndCreateTokensEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextCombatDamageByTargetCreatureAndCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (targetId == null || controllerId == null) return;

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) return;

        PreventNextCombatDamageByTargetCreatureAndCreateTokensEffect prevention =
                (PreventNextCombatDamageByTargetCreatureAndCreateTokensEffect) effect;
        gameData.sourceNextDamageToAnyTargetShields.add(
                SourceNextDamageToAnyTargetShield.combatPlayerPreventionWithTokens(
                        targetId, prevention.token(), controllerId, entry.getCard().getSetCode(),
                        gameData.combatPhasesThisTurn));
        gameLogService.append(gameData, GameLog.textCardText(
                "The next time ", target.getCard(),
                " would deal combat damage to a player this combat, it is prevented. "
                        + "Create one token for each damage prevented."));
    }
}
