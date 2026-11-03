package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "120")
public class KamiOfTheWaningMoon extends Card {

    public KamiOfTheWaningMoon() {
        // Whenever you cast a Spirit or Arcane spell, target creature gains fear until end of
        // turn. The target is chosen as the trigger goes on the stack.
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE)))),
                new GrantKeywordEffect(Keyword.FEAR, GrantScope.TARGET));
    }
}
