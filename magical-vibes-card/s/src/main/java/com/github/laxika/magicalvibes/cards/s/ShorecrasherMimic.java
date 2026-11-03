package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "159")
public class ShorecrasherMimic extends Card {

    public ShorecrasherMimic() {
        // Whenever you cast a spell that's both green and blue, this creature has base power and
        // toughness 5/3 until end of turn and gains trample until end of turn.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardColorPredicate(CardColor.GREEN),
                                new CardColorPredicate(CardColor.BLUE)))),
                new SetBasePowerToughnessEffect(5, 3, GrantScope.SELF),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF));
    }
}
