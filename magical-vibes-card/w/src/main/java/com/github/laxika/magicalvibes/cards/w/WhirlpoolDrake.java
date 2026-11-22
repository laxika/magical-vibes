package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "34")
public class WhirlpoolDrake extends Card {

    public WhirlpoolDrake() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                EachPlayerShufflesZonesIntoLibraryEffect.handsOf(Subjects.you()),
                new DrawCardEffect(AmountThisWay.shuffledAwayByYou())));
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                EachPlayerShufflesZonesIntoLibraryEffect.handsOf(Subjects.you()),
                new DrawCardEffect(AmountThisWay.shuffledAwayByYou())));
    }
}
