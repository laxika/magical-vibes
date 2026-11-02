package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "26")
public class Betrayal extends Card {

    public Betrayal() {
        // Enchant creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls());
        // Whenever enchanted creature becomes tapped, you draw a card.
        addTrigger(Triggers.enchantedPermanentBecomesTapped(), new DrawCardEffect(1));
    }
}
