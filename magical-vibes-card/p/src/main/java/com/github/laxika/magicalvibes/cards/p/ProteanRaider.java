package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "RIX", collectorNumber = "167")
public class ProteanRaider extends Card {

    public ProteanRaider() {
        // Raid — If you attacked this turn, you may have this creature enter as a copy of any
        // creature on the battlefield.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalReplacementEffect(new Raid(),
                new CopyPermanentOnEnterEffect(new PermanentIsCreaturePredicate(), "creature")));
    }
}
