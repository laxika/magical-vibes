package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ICE", collectorNumber = "157")
public class PestilenceRats extends Card {

    public PestilenceRats() {
        // Pestilence Rats's power is equal to the number of other Rats on the battlefield.
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.RAT),
                        CountScope.ANY_PLAYER, true),
                new Fixed(3)));
    }
}
