package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "USG", collectorNumber = "329")
public class ThranQuarry extends Card {

    public ThranQuarry() {
        // At the beginning of the end step, if you control no creatures, sacrifice this land.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCountAtMost(0, new PermanentIsCreaturePredicate()),
                new SacrificeSelfEffect()));

        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
