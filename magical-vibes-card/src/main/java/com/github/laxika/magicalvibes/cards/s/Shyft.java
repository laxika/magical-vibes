package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ICE", collectorNumber = "96")
public class Shyft extends Card {

    public Shyft() {
        // At the beginning of your upkeep, you may have this creature become the color or colors
        // of your choice. (This effect lasts indefinitely.)
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new BecomeChosenColorsIndefinitelyEffect(),
                "Have this creature become the color or colors of your choice?"
        ));
    }
}
