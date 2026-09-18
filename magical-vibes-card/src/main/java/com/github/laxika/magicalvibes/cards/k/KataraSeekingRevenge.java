package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.WaterbendCostPaid;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "TLE", collectorNumber = "148")
public class KataraSeekingRevenge extends Card {

    public KataraSeekingRevenge() {
        addEffect(EffectSlot.SPELL, WaterbendCost.optional(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DrawCardEffect(1),
                new ConditionalEffect(
                        new NotCondition(new WaterbendCostPaid()),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER))));

        CardsInGraveyard lessons = new CardsInGraveyard(
                new CardSubtypePredicate(CardSubtype.LESSON), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(lessons, lessons));
    }
}
