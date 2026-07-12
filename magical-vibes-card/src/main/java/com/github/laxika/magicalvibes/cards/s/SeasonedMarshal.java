package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "9ED", collectorNumber = "42")
@CardRegistration(set = "8ED", collectorNumber = "44")
@CardRegistration(set = "POR", collectorNumber = "26")
public class SeasonedMarshal extends Card {

    public SeasonedMarshal() {
        // Whenever this creature attacks, you may tap target creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                "Tap target creature?"
        ));
    }
}
