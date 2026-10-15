package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SNC", collectorNumber = "63")
public class UndercoverOperative extends Card {

    public UndercoverOperative() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                CopyPermanentOnEnterEffect.withShieldCounterIfControllerControlsCopiedPermanent(
                        new PermanentIsCreaturePredicate(), "creature"));
    }
}
