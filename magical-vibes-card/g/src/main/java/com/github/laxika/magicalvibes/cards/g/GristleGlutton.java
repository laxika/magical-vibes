package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "144")
public class GristleGlutton extends Card {

    public GristleGlutton() {
        // "{T}, Blight 1: Discard a card. If you do, draw a card."
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PutCounterOnControlledCreatureCost.blight(1),
                        SequenceEffect.of(
                                new DiscardEffect(1, Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.discarded(),
                                        new DrawCardEffect(1)))),
                "{T}, Blight 1: Discard a card. If you do, draw a card."
        ));
    }
}
