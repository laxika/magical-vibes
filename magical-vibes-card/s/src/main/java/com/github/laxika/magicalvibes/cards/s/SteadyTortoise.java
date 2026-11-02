package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HarriedDash;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "25")
public class SteadyTortoise extends Card {

    public SteadyTortoise() {
        setBackFaceCard(new HarriedDash());
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
        addCastingOption(new AdventureCast("{R}"));

        PerpetuallyBoostCardEffect attackBoost = new PerpetuallyBoostCardEffect(this, 1, 1);
        addTrigger(Triggers.youAttack(), attackBoost);
        addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.EXILE), TriggerOptions.DEFAULT, attackBoost);
    }

    @Override
    public String getBackFaceClassName() {
        return "HarriedDash";
    }
}
