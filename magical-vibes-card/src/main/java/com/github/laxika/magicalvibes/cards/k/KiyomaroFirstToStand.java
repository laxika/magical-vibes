package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "SOK", collectorNumber = "18")
public class KiyomaroFirstToStand extends Card {

    public KiyomaroFirstToStand() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(cardsInHand, cardsInHand));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CardsInHandAtLeast(4),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)));
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new ConditionalEffect(
                new CardsInHandAtLeast(7), new GainLifeEffect(7)));
    }
}
