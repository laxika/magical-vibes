package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DSC", collectorNumber = "365")
public class YourNightmaresAreDelicious extends Card {

    public YourNightmaresAreDelicious() {
        // When you set this scheme in motion, each opponent who has more than five cards in hand discards cards equal
        // to the difference. Then if fewer than three cards were discarded this way, you draw three cards.
        addSpellEffect(DiscardEffect.eachEvaluatedFor(
                new Max(new Fixed(0), new Sum(new CardsInHand(CountScope.CONTROLLER), new Fixed(-5))),
                Subjects.eachOpponent()));
        addSpellEffect(new ConditionalEffect(new NotCondition(new HappenedThisWay(StepKind.DISCARD, 3)),
                new DrawCardEffect(3)));
    }
}
