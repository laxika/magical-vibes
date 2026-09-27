package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;

@CardRegistration(set = "40K", collectorNumber = "118")
public class EpistolaryLibrarian extends Card {

    public EpistolaryLibrarian() {
        addEffect(EffectSlot.ON_ATTACK,
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(new XValue()));
    }
}
