package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.TapCombatOpponentsAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link TapCombatOpponentsOfTargetAtEndOfCombatEffect}. The spell's form records its
 * targets in one {@link TapCombatOpponentsAtEndOfCombat} delayed trigger; the delayed trigger's form
 * taps each creature blocked by one of those creatures this turn and keeps it from untapping during
 * its controller's next untap step.
 */
@Component
@RequiredArgsConstructor
public class TapCombatOpponentsOfTargetAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapCombatOpponentsOfTargetAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (((TapCombatOpponentsOfTargetAtEndOfCombatEffect) effect).schedule()) {
            gameData.queueDelayedAction(new TapCombatOpponentsAtEndOfCombat(
                    List.copyOf(entry.targetsForEffect(effect)), entry.getCard(), entry.getControllerId()));
            return;
        }
        Set<UUID> blockedIds = new LinkedHashSet<>();
        for (UUID creatureId : entry.getTriggeringPermanentIds()) {
            blockedIds.addAll(gameData.combatOpponentIdsBlockedByThisTurn.getOrDefault(creatureId, Set.of()));
        }
        for (UUID blockedId : blockedIds) {
            Permanent blocked = gameQueryService.findPermanentById(gameData, blockedId);
            if (blocked == null) {
                continue;
            }
            tapUntapSupport.tapPermanent(gameData, blocked);
            blocked.setSkipUntapCount(Math.max(blocked.getSkipUntapCount(), 1));
            gameLogService.append(gameData, GameLog.cardThen(blocked.getCard(),
                    " is tapped and doesn't untap during its controller's next untap step."));
        }
    }
}
