package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "717")
@CardRegistration(set = "CMM", collectorNumber = "750")
public class SkitteringCicada extends Card {

    public SkitteringCicada() {
        addStaticEffect(new GrantFlashToCardTypeEffect(new CardIsColorlessPredicate()));
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsColorlessPredicate()),
                new BoostSelfEffect(new TargetSpellManaValue(), new TargetSpellManaValue()),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF));
    }
}
