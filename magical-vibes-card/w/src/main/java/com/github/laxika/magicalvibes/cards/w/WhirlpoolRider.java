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

@CardRegistration(set = "APC", collectorNumber = "35")
public class WhirlpoolRider extends Card {

    public WhirlpoolRider() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                EachPlayerShufflesZonesIntoLibraryEffect.handsOf(Subjects.you()),
                new DrawCardEffect(AmountThisWay.shuffledAwayByYou())));
    }
}
