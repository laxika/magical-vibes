package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "BNG", collectorNumber = "13")
public class GlimpseTheSunGod extends Card {

    public GlimpseTheSunGod() {
        targetExactlyX(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Targets must be creatures"
        ), 100)
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new ScryEffect(1));
    }
}
