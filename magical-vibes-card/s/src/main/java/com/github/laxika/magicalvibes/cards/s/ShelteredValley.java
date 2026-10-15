package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherPermanentsWithSameNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ALL", collectorNumber = "142")
public class ShelteredValley extends Card {

    public ShelteredValley() {
        // If this land would enter, instead sacrifice each other permanent named Sheltered Valley
        // you control, then put this land onto the battlefield.
        addEffect(EffectSlot.STATIC, new SacrificeOtherPermanentsWithSameNameOnEnterEffect());

        // At the beginning of your upkeep, if you control three or fewer lands, you gain 1 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCountAtMost(3, new PermanentIsLandPredicate()), new GainLifeEffect(1)));

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
