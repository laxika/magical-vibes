package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RevealCardsChooseOneToDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;

@CardRegistration(set = "LGN", collectorNumber = "75")
public class HollowSpecter extends Card {

    public HollowSpecter() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        EachPlayerPaysAnyManaEffect.byYou(Resource.X_MANA),
                        new ConditionalEffect(new HappenedThisWay(StepKind.PAY_MANA),
                                new RevealCardsChooseOneToDiscardEffect(AmountThisWay.manaPaid()))));
    }
}
