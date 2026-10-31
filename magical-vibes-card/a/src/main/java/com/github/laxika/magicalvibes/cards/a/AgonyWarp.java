package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ALA", collectorNumber = "153")
@CardRegistration(set = "MM2", collectorNumber = "170")
@CardRegistration(set = "MM3", collectorNumber = "150")
@CardRegistration(set = "2X2", collectorNumber = "168")
@CardRegistration(set = "ARC", collectorNumber = "76")
public class AgonyWarp extends Card {

    public AgonyWarp() {
        // Both effects may target the same creature (it then gets -3/-3 until end of turn).
        setAllowSharedTargets(true);

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature"
        )).addSpellEffect(new BoostTargetCreatureEffect(-3, 0));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Second target must be a creature"
        )).addSpellEffect(new BoostTargetCreatureEffect(0, -3));
    }
}
