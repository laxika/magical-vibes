package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;

@CardRegistration(set = "PIP", collectorNumber = "48")
@CardRegistration(set = "PIP", collectorNumber = "576")
public class RuthlessRadrat extends Card {

    public RuthlessRadrat() {
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.graveyardExile(4));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{0}")));
    }
}
