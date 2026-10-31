package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "AKH", collectorNumber = "25")
@CardRegistration(set = "AKR", collectorNumber = "32")
@CardRegistration(set = "A25", collectorNumber = "31")
@CardRegistration(set = "ONS", collectorNumber = "50")
@CardRegistration(set = "VMA", collectorNumber = "42")
@CardRegistration(set = "DMR", collectorNumber = "23")
@CardRegistration(set = "DMR", collectorNumber = "269")
public class RenewedFaith extends Card {

    public RenewedFaith() {
        // You gain 6 life.
        addSpellEffect(new GainLifeEffect(6));

        // When you cycle this card, you may gain 2 life. The trigger is separate from the cycling
        // draw, so it sits above the draw on the stack and the draw is never blocked by it.
        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(new GainLifeEffect(2), "Gain 2 life?"));

        addCycling("{1}{W}");
    }
}
