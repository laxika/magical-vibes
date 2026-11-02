package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "264")
public class RushwoodElemental extends Card {

    public RushwoodElemental() {
        // At the beginning of your upkeep, you may put a +1/+1 counter on this creature.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new PutCountersOnSourceEffect(1, 1, 1),
                "Put a +1/+1 counter on Rushwood Elemental?"));
    }
}
