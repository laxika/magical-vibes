package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsUpkeepSacrificeUnlessPayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "SOK", collectorNumber = "14")
public class KatakiWarsWage extends Card {

    public KatakiWarsWage() {
        // All artifacts have "At the beginning of your upkeep, sacrifice this artifact unless you pay {1}."
        addEffect(EffectSlot.STATIC,
                new AllPermanentsUpkeepSacrificeUnlessPayEffect(new PermanentIsArtifactPredicate(), "{1}"));
    }
}
