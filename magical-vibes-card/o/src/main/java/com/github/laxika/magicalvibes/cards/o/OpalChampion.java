package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ULG", collectorNumber = "16")
public class OpalChampion extends Card {

    public OpalChampion() {
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()),
                new BecomeCreatureEffect(3, 3, CardSubtype.KNIGHT, Set.of(Keyword.FIRST_STRIKE)));
    }
}
