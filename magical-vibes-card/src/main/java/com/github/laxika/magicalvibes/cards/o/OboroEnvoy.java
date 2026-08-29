package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "49")
public class OboroEnvoy extends Card {

    public OboroEnvoy() {
        // {2}, Return a land you control to its owner's hand: Target creature gets -X/-0 until
        // end of turn, where X is the number of cards in your hand.
        CardsInHand handSize = new CardsInHand(CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        new BoostTargetCreatureEffect(new Scaled(handSize, -1), new Fixed(0))),
                "{2}, Return a land you control to its owner's hand: Target creature gets -X/-0 until end of turn, where X is the number of cards in your hand.",
                TargetFilters.creature()));
    }
}
