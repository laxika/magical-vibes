package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "11")
public class LoyalGyrfalcon extends Card {

    public LoyalGyrfalcon() {
        // Whenever you cast a white spell, this creature loses defender until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.WHITE)),
                new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF));
    }
}
