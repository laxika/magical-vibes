package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "35")
public class ThunderousOrator extends Card {

    public ThunderousOrator() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                grantIfControlledCreatureHas(Keyword.FLYING),
                grantIfControlledCreatureHas(Keyword.FIRST_STRIKE),
                grantIfControlledCreatureHas(Keyword.DOUBLE_STRIKE),
                grantIfControlledCreatureHas(Keyword.DEATHTOUCH),
                grantIfControlledCreatureHas(Keyword.INDESTRUCTIBLE),
                grantIfControlledCreatureHas(Keyword.LIFELINK),
                grantIfControlledCreatureHas(Keyword.MENACE),
                grantIfControlledCreatureHas(Keyword.TRAMPLE)));
    }

    private static CardEffect grantIfControlledCreatureHas(Keyword keyword) {
        return new ConditionalEffect(
                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(keyword)))),
                new GrantKeywordEffect(keyword, GrantScope.SELF));
    }
}
