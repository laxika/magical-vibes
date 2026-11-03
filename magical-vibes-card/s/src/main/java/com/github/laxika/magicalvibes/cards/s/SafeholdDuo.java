package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "238")
public class SafeholdDuo extends Card {

    public SafeholdDuo() {
        // Whenever you cast a green spell, this creature gets +1/+1 until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.GREEN)),
                new BoostSelfEffect(1, 1));

        // Whenever you cast a white spell, this creature gains vigilance until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.WHITE)),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF));
    }
}
