package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ODY", collectorNumber = "180")
public class BurningSands extends Card {

    public BurningSands() {
        // Whenever a creature dies, that creature's controller sacrifices a land of their choice.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new DyingCreatureControllerSacrificesPermanentsEffect(1, new PermanentIsLandPredicate()));
    }
}
