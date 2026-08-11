package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinDoubleOrPreventNextCombatDamageFromAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Impulsive Maneuvers's per-attacker coin flip and installs its one-shot combat shield. */
@Component
@RequiredArgsConstructor
public class FlipCoinDoubleOrPreventNextCombatDamageFromAttackingCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinDoubleOrPreventNextCombatDamageFromAttackingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackerId = entry.getTargetId();
        if (attackerId == null) return;

        boolean wonFlip = ThreadLocalRandom.current().nextBoolean();
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        gameLogService.append(gameData, GameLog.text(wonFlip
                ? gameData.playerIdToName.get(controllerId) + " wins the coin flip for " + sourceName + "."
                : gameData.playerIdToName.get(controllerId) + " loses the coin flip for " + sourceName + "."));
        if (wonFlip) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
        }

        gameData.sourceNextDamageToAnyTargetShields.add(wonFlip
                ? SourceNextDamageToAnyTargetShield.combatDoubling(attackerId)
                : SourceNextDamageToAnyTargetShield.combatPrevention(attackerId));
    }
}
