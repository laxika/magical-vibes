package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "708")
public class WandasVision extends Card {

    public WandasVision() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), SequenceEffect.of(
                ExileCardsFromTopUntilEffect.untilNonland(Subjects.you()),
                new MayCastDiscardedCardWithoutPayingManaCostEffect(Subjects.cardsThisWay(StepKind.EXILE))));
    }
}
