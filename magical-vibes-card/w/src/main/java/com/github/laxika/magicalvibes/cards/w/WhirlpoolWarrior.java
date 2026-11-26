package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "36")
@CardRegistration(set = "PC2", collectorNumber = "29")
@CardRegistration(set = "PCA", collectorNumber = "29")
public class WhirlpoolWarrior extends Card {

    public WhirlpoolWarrior() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                EachPlayerShufflesZonesIntoLibraryEffect.handsOf(Subjects.you()),
                new DrawCardEffect(AmountThisWay.shuffledAwayByYou())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), SequenceEffect.of(
                        EachPlayerShufflesZonesIntoLibraryEffect.handsOf(Subjects.eachPlayer()),
                        DrawCardEffect.eachEvaluatedFor(AmountThisWay.shuffledAwayByYou(), Subjects.eachPlayer()))),
                "{R}, Sacrifice this creature: Each player shuffles the cards from their hand into their library, then draws that many cards."
        ));
    }
}
