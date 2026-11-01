package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "158")
@CardRegistration(set = "MKM", collectorNumber = "351")
public class CulvertAmbusher extends Card {

    public CulvertAmbusher() {
        addMorph("{4}{G}");
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BLOCK))
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BLOCK));
    }
}
