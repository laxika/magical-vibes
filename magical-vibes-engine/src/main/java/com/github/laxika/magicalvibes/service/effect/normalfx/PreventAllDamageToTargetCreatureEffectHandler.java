package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventAllDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventAllDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        boolean combatOnly = ((PreventAllDamageToTargetCreatureEffect) effect).combatOnly();
        // Multi-target: shield each valid creature in this effect's target group (e.g. Redeem's
        // "up to two target creatures"). Falls back to the single target for one-target spells/abilities.
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (!targetIds.isEmpty()) {
            for (UUID targetId : targetIds) {
                shieldTarget(gameData, targetId, combatOnly);
            }
            return;
        }

        shieldTarget(gameData, entry.getTargetId(), combatOnly);
    }

    private void shieldTarget(GameData gameData, UUID targetId, boolean combatOnly) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        String logEntry;
        if (combatOnly) {
            gameData.creaturesWithCombatDamagePrevented.add(targetId);
            logEntry = "All combat damage that would be dealt to " + target.getCard().getName() + " this turn is prevented.";
        } else {
            gameData.creaturesWithAllDamagePrevented.add(targetId);
            logEntry = "All damage that would be dealt to " + target.getCard().getName() + " this turn is prevented.";
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
    }
}
