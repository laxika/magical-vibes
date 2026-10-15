package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "SOS", collectorNumber = "2")
public class RancorousArchaic extends Card {

    public RancorousArchaic() {
        // Converge — This creature enters with a +1/+1 counter on it for each color of mana spent
        // to cast it. The Converge keyword (from Scryfall) makes SpellCastingService snapshot the
        // colors spent and carry the count as the stack entry's X.
        // Trample and reach come from Scryfall metadata.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
