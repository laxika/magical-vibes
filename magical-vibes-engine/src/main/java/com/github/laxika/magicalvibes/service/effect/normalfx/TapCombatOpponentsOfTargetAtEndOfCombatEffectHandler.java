package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link TapCombatOpponentsOfTargetAtEndOfCombatEffect}. The spell's form registers one delayed
 * triggered ability about its targets for this turn's next end of combat; the delayed ability's form
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
            List<UUID> creatureIds = List.copyOf(entry.targetsForEffect(effect));
            if (creatureIds.isEmpty()) {
                return;
            }
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStepThisTurn(
                    Triggers.endOfCombat(), entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(),
                    creatureIds.getFirst(), DelayedReferents.permanents(creatureIds),
                    new TapCombatOpponentsOfTargetAtEndOfCombatEffect(false)));
            return;
        }
        Set<UUID> blockedIds = new LinkedHashSet<>();
        for (UUID creatureId : entry.getDeclaredTargetIds()) {
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
