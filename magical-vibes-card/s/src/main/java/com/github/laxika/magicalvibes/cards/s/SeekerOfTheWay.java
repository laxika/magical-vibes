package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "22")
@CardRegistration(set = "CP3", collectorNumber = "2")
@CardRegistration(set = "IMA", collectorNumber = "29")
@CardRegistration(set = "2X2", collectorNumber = "28")
public class SeekerOfTheWay extends Card {

    public SeekerOfTheWay() {
        // Whenever you cast a noncreature spell, this creature gains lifelink until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF));
    }
}
