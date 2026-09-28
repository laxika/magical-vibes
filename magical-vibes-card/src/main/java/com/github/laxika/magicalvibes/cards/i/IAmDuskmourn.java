package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "DSC", collectorNumber = "334")
public class IAmDuskmourn extends Card {

    public IAmDuskmourn() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                        null, null, new SacrificeSelfEffect()));
    }
}
