package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Removes each permanent the effect's subject names from combat through {@link CombatRemovalSupport}; a permanent
 * that left the battlefield is not affected.
 */
@Component
@RequiredArgsConstructor
public class RemoveTargetFromCombatEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final CombatRemovalSupport combatRemovalSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveTargetFromCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var removal = (RemoveTargetFromCombatEffect) effect;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, removal.subject())) {
            if (removal.unblockSoleBlockedAttackers()) {
                combatRemovalSupport.removeFromCombatAndUnblockSoleBlockers(gameData, entry, permanent);
            } else {
                combatRemovalSupport.removeFromCombat(gameData, entry, permanent);
            }
        }
    }
}
