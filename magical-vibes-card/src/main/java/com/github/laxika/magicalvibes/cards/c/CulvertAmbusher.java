package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "158")
public class CulvertAmbusher extends Card {

    public CulvertAmbusher() {
        addMorph("{4}{G}");
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BLOCK))
                .addEffect(EffectSlot.ON_TURNED_FACE_UP,
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BLOCK));
    }
}
