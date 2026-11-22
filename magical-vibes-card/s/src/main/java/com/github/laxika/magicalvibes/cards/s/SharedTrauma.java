package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "CMD", collectorNumber = "99")
@CardRegistration(set = "CM2", collectorNumber = "76")
public class SharedTrauma extends Card {

    public SharedTrauma() {
        addSpellEffect(SequenceEffect.of(
                EachPlayerPaysAnyManaEffect.joinForces(),
                new MillEffect(AmountThisWay.manaPaid(), Subjects.eachPlayer())));
    }
}
