package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "110")
public class StirTheSands extends Card {

    public StirTheSands() {
        // Create three 2/2 black Zombie creature tokens.
        addSpellEffect(CreateTokenEffect.blackZombie(3));

        // Cycling {3}{B}; when you cycle this card, create a 2/2 black Zombie creature token. The
        // trigger is separate from the cycling draw and goes on the stack above it.
        addTrigger(Triggers.youCycleThisCard(), CreateTokenEffect.blackZombie(1));
        addCycling("{3}{B}");
    }
}
