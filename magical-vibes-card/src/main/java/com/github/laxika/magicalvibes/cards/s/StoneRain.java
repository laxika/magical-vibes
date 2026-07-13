package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "209")
@CardRegistration(set = "7ED", collectorNumber = "221")
@CardRegistration(set = "8ED", collectorNumber = "225")
@CardRegistration(set = "9ED", collectorNumber = "221")
@CardRegistration(set = "POR", collectorNumber = "151")
@CardRegistration(set = "P02", collectorNumber = "117")
@CardRegistration(set = "PTK", collectorNumber = "123")
public class StoneRain extends Card {

    public StoneRain() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
