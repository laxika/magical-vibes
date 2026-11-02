package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "181")
public class UrbanDaggertooth extends Card {

    public UrbanDaggertooth() {
        // Enrage — Whenever this creature is dealt damage, proliferate.
        addTrigger(Triggers.dealtDamage(), new ProliferateEffect());
    }
}
