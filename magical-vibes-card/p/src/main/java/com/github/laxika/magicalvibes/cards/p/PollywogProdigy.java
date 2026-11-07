package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "15")
@CardRegistration(set = "BLC", collectorNumber = "50")
public class PollywogProdigy extends Card {

    public PollywogProdigy() {
        // Evolve is the keyword, which the ally-creature entry collector triggers.
        addTrigger(Triggers.opponentCastsSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                new CardManaValueLessThanSourcePowerPredicate()
                        ))),
                new DrawCardEffect());
    }
}
