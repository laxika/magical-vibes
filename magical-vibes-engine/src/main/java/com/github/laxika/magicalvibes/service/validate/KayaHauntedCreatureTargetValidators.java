package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.effect.DealDamageToOwnerOfTargetHauntedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetHauntedCreatureEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Target validators for Kaya's emblem effects, whose haunted restriction is game-state data. */
@Service
@RequiredArgsConstructor
public class KayaHauntedCreatureTargetValidators {

    private final TargetValidationService targetValidationService;

    @ValidatesTarget(DealDamageToOwnerOfTargetHauntedCreatureEffect.class)
    public void validateDamageTarget(TargetValidationContext context,
                                     DealDamageToOwnerOfTargetHauntedCreatureEffect effect) {
        requireHauntedCreature(context);
    }

    @ValidatesTarget(GainControlOfTargetHauntedCreatureEffect.class)
    public void validateControlTarget(TargetValidationContext context,
                                      GainControlOfTargetHauntedCreatureEffect effect) {
        requireHauntedCreature(context);
    }

    private void requireHauntedCreature(TargetValidationContext context) {
        targetValidationService.requireTarget(context);
        if (!context.gameData().hauntingCardToPermanentId.containsValue(context.targetId())) {
            throw new IllegalStateException("Target must be a haunted creature");
        }
    }
}
