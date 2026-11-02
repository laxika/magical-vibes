package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "73")
public class AvengerOfTheFallen extends Card {

    public AvengerOfTheFallen() {
        addTrigger(Triggers.attacks(),
                com.github.laxika.magicalvibes.model.effect.SequenceEffect.of(new CreateTokenEffect(
                CardType.CREATURE,
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                "Warrior", 1, 1, CardColor.RED, null, List.of(CardSubtype.WARRIOR),
                Set.of(), Set.of(), true, false, List.of(), List.of(), false, false, false, 0, Set.of()
        ), new SacrificeCreatedPermanentsAtEndStepEffect()));
    }
}
