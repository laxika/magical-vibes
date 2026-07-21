package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "INR", collectorNumber = "20")
public class DrogskolShieldmate extends Card {

    public DrogskolShieldmate() {
        // When this creature enters, other creatures you control get +0/+1 until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllOwnCreaturesEffect(
                0, 1, new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
    }
}
