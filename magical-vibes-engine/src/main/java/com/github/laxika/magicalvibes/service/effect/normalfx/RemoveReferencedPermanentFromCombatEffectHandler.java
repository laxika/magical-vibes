package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveReferencedPermanentFromCombatEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RemoveReferencedPermanentFromCombatEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final CombatRemovalSupport combatRemovalSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveReferencedPermanentFromCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RemoveReferencedPermanentFromCombatEffect referencedEffect =
                (RemoveReferencedPermanentFromCombatEffect) effect;
        Permanent permanent = subjectResolutionService.permanent(
                gameData, entry, effect, Subjects.of(referencedEffect.reference()));
        if (permanent == null) {
            return;
        }
        if (referencedEffect.unblockSoleBlockedAttackers()) {
            combatRemovalSupport.removeFromCombatAndUnblockSoleBlockers(gameData, entry, permanent);
        } else {
            combatRemovalSupport.removeFromCombat(gameData, entry, permanent);
        }
    }
}
