package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersPerSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;

@CardRegistration(set = "ISD", collectorNumber = "121")
public class UnbreathingHorde extends Card {

    public UnbreathingHorde() {
        // Unbreathing Horde enters the battlefield with a +1/+1 counter on it for each
        // other Zombie you control and each Zombie card in your graveyard.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithPlusOnePlusOneCountersPerSubtypeEffect(CardSubtype.ZOMBIE, true));

        // If damage would be dealt to Unbreathing Horde, prevent that damage and
        // remove a +1/+1 counter from it.
        addEffect(EffectSlot.STATIC, new PreventDamageAndRemovePlusOnePlusOneCountersEffect(true));
    }
}
