package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "219")
public class KamiOfTheHunt extends Card {

    public KamiOfTheHunt() {
        // Whenever you cast a Spirit or Arcane spell, this creature gets +1/+1 until end of turn.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE)))),
                new BoostSelfEffect(1, 1));
    }
}
