package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "AFC", collectorNumber = "15")
public class DivinersPortent extends Card {

    public DivinersPortent() {
        addEffect(EffectSlot.SPELL, RollD20Effect.withAddedAmount(
                new CardsInHand(CountScope.CONTROLLER),
                new DrawCardEffect(new XValue()),
                SequenceEffect.of(
                        new ScryEffect(new XValue()),
                        new DrawCardEffect(new XValue()))));
    }
}
