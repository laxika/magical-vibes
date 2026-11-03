package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "218")
public class TattermungeDuo extends Card {

    public TattermungeDuo() {
        // Whenever you cast a red spell, this creature gets +1/+1 until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.RED)),
                new BoostSelfEffect(1, 1));

        // Whenever you cast a green spell, this creature gains forestwalk until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.GREEN)),
                new GrantKeywordEffect(Keyword.FORESTWALK, GrantScope.SELF));
    }
}
