package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "104")
public class GoblinGathering extends Card {

    public GoblinGathering() {
        CardsInGraveyard namedCardsInGraveyard = new CardsInGraveyard(
                new CardNamedPredicate("Goblin Gathering"), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new Sum(new Fixed(2), namedCardsInGraveyard),
                "Goblin",
                1,
                1,
                CardColor.RED,
                List.of(CardSubtype.GOBLIN),
                Set.of(),
                Set.of()
        ));
    }
}
