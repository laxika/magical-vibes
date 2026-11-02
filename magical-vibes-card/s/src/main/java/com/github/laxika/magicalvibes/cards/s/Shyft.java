package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "96")
@CardRegistration(set = "ME2", collectorNumber = "66")
public class Shyft extends Card {

    public Shyft() {
        // At the beginning of your upkeep, you may have this creature become the color or colors
        // of your choice. (This effect lasts indefinitely.)
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new BecomeChosenColorsIndefinitelyEffect(),
                "Have this creature become the color or colors of your choice?"
        ));
    }
}
