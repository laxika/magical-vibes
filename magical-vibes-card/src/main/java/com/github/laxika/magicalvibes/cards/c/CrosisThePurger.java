package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAllCardsOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "INV", collectorNumber = "242")
public class CrosisThePurger extends Card {

    public CrosisThePurger() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayPayManaEffect("{2}{B}", DiscardAllCardsOfChosenColorEffect.damagedPlayer(),
                        "Pay {2}{B}?"));
    }
}
