package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;

import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "60")
@CardRegistration(set = "C13", collectorNumber = "216")
public class SpellbreakerBehemoth extends Card {

    public SpellbreakerBehemoth() {
        // "This spell can't be countered." — protects its own spell, which the static ability below
        // cannot (that only functions once the Behemoth is a permanent on the battlefield).
        addStaticEffect(new CantBeCounteredEffect());
        // "Creature spells you control with power 5 or greater can't be countered."
        addStaticEffect(new ControllerSpellsCantBeCounteredEffect(false, null, Set.of(CardType.CREATURE),
                new CardPowerAtLeastPredicate(5)));
    }
}
