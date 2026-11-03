package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "185")
public class EmberstrikeDuo extends Card {

    public EmberstrikeDuo() {
        // Whenever you cast a black spell, this creature gets +1/+1 until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.BLACK)),
                new BoostSelfEffect(1, 1));

        // Whenever you cast a red spell, this creature gains first strike until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.RED)),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF));
    }
}
