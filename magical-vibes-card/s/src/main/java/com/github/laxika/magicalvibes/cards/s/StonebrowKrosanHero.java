package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "247")
public class StonebrowKrosanHero extends Card {

    public StonebrowKrosanHero() {
        // Whenever a creature you control with trample attacks, it gets +2/+2 until end of turn.
        addTrigger(Triggers.creatureYouControlAttacks(), new TriggeringPermanentConditionalEffect(
                new PermanentHasKeywordPredicate(Keyword.TRAMPLE),
                new BoostTargetCreatureEffect(2, 2)));
    }
}
