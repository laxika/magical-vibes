package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "48")
public class FateForetold extends Card {

    public FateForetold() {
        // Enchant creature
        target(TargetFilters.creature())
                // When this Aura enters, draw a card.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect())
                // When enchanted creature dies, its controller draws a card.
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new DrawCardForTargetPlayerEffect(1));
    }
}
