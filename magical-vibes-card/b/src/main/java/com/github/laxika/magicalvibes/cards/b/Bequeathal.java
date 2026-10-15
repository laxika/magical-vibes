package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EXO", collectorNumber = "106")
public class Bequeathal extends Card {

    public Bequeathal() {
        // Enchant creature
        target(TargetFilters.creature());

        // When enchanted creature dies, you draw two cards.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new DrawCardEffect(2));
    }
}
