package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicatesOfTargetPermanentsIntoHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "HBG", collectorNumber = "38")
public class SnowbornSimulacra extends Card {

    public SnowbornSimulacra() {
        targetExactlyX(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                "Targets must be nontoken permanents"), 100)
                .addEffect(EffectSlot.SPELL, new ConjureDuplicatesOfTargetPermanentsIntoHandEffect());
    }
}
