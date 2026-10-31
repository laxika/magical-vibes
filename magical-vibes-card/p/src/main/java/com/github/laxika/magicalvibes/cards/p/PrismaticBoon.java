package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "MIR", collectorNumber = "274")
public class PrismaticBoon extends Card {

    public PrismaticBoon() {
        // Choose a color. X target creatures gain protection from the chosen color until end of turn.
        // One color choice on resolution covers the whole X-scaled target group.
        targetExactlyX(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Targets must be creatures"
        ), Integer.MAX_VALUE)
                .addSpellEffect(new GrantProtectionChoiceUntilEndOfTurnEffect());
    }
}
