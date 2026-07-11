package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "PTK", collectorNumber = "82")
public class SimaYiWeiFieldMarshal extends Card {

    public SimaYiWeiFieldMarshal() {
        PermanentCount swampsYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER);
        // Power is equal to the number of Swamps you control; toughness stays 4.
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(swampsYouControl, new Fixed(4)));
    }
}
