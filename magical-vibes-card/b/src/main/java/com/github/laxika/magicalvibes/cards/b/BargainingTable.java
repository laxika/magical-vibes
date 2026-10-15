package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivationCostEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "288")
public class BargainingTable extends Card {

    public BargainingTable() {
        addActivatedAbility(new ActivatedAbility(true, "{0}", List.of(
                new IncreaseActivationCostEffect(new CardsInHand(CountScope.OPPONENTS)),
                new DrawCardEffect(1)),
                "{X}, {T}: Draw a card. X is the number of cards in an opponent's hand."));
    }
}
