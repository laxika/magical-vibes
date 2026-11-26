package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "CMD", collectorNumber = "51")
@CardRegistration(set = "CMM", collectorNumber = "105")
@CardRegistration(set = "CMM", collectorNumber = "491")
@CardRegistration(set = "C16", collectorNumber = "93")
@CardRegistration(set = "CM2", collectorNumber = "46")
public class MindsAglow extends Card {

    public MindsAglow() {
        addSpellEffect(SequenceEffect.of(
                EachPlayerPaysAnyManaEffect.joinForces(),
                new DrawCardEffect(AmountThisWay.manaPaid(), Subjects.eachPlayer())));
    }
}
