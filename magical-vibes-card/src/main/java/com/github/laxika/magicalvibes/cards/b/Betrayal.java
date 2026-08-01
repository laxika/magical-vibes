package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VIS", collectorNumber = "26")
public class Betrayal extends Card {

    public Betrayal() {
        // Enchant creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls());
        // Whenever enchanted creature becomes tapped, you draw a card.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, new DrawCardEffect(1));
    }
}
