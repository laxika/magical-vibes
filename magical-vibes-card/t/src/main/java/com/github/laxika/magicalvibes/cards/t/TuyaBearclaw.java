package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "CMM", collectorNumber = "360")
public class TuyaBearclaw extends Card {

    public TuyaBearclaw() {
        // Whenever Tuya Bearclaw attacks, it gets +X/+X until end of turn, where X is the
        // greatest power among other creatures you control.
        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate());
        var greatestOtherPower = new GreatestPowerAmongControlled(otherCreatures);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(greatestOtherPower, greatestOtherPower));
    }
}
