package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "GTC", collectorNumber = "175")
public class MartialGlory extends Card {

    public MartialGlory() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature"
        )).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 0));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Second target must be a creature"
        )).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(0, 3));

        setAllowSharedTargets(true);
    }
}
