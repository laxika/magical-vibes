package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndPreventCombatDamageByTargetAndBlockersEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves the targeted attacker and its blockers' combat-damage prevention. */
@Component
@RequiredArgsConstructor
public class TapAndPreventCombatDamageByTargetAndBlockersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapAndPreventCombatDamageByTargetAndBlockersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent attacker = gameQueryService.findPermanentById(gameData, targetId);
        if (attacker == null || !gameQueryService.isCreature(gameData, attacker) || !attacker.isAttacking()) {
            return;
        }

        List<Permanent> blockers = new ArrayList<>();
        Set<UUID> preventedCreatureIds = new HashSet<>();
        preventedCreatureIds.add(attacker.getId());
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && permanent.isBlocking()
                    && permanent.getBlockingTargetIds().contains(attacker.getId())) {
                blockers.add(permanent);
                preventedCreatureIds.add(permanent.getId());
            }
        });

        for (UUID preventedId : preventedCreatureIds) {
            PreventionSupport.preventCombatDamageDealtBy(gameData, entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), preventedId, Duration.UNTIL_END_OF_TURN);
        }
        for (Permanent blocker : blockers) {
            tapUntapSupport.tapPermanent(gameData, blocker);
        }

        gameLogService.append(gameData, GameLog.text(
                "Combat damage this turn from " + FaceDownVisibility.publicName(gameData, attacker)
                        + " and its blockers will be prevented."));
    }
}
