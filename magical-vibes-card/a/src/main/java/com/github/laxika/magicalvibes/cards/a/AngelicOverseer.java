package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "3")
public class AngelicOverseer extends Card {

    public AngelicOverseer() {
        addStaticEffect(new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)), new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)), new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));
    }
}
