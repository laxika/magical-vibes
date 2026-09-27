package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "CMM", collectorNumber = "723")
@CardRegistration(set = "CMM", collectorNumber = "756")
public class OnduSpiritdancer extends Card {

    public OnduSpiritdancer() {
        // Whenever an enchantment you control enters, you may create a token that's a copy of it.
        // Do this only once each turn.
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new OncePerTurnTriggerEffect(new MayEffect(
                        new CreateTokenCopyOfEnteringPermanentEffect(),
                        "Create a token that's a copy of that enchantment?")));
    }
}
