package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "APC", collectorNumber = "2")
public class CoalitionFlag extends Card {

    public CoalitionFlag() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.STATIC,
                        new GrantSubtypeEffect(CardSubtype.FLAGBEARER, GrantScope.ENCHANTED_CREATURE));
    }
}
