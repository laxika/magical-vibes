package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

@CardRegistration(set = "5ED", collectorNumber = "188")
@CardRegistration(set = "4ED", collectorNumber = "154")
public class PlagueRats extends Card {

    public PlagueRats() {
        PermanentCount plagueRats =
                new PermanentCount(new PermanentNamedPredicate("Plague Rats"), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(plagueRats, plagueRats));
    }
}
