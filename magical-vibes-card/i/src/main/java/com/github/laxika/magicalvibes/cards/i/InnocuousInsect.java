package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "MB1", collectorNumber = "23")
public class InnocuousInsect extends Card {

    public InnocuousInsect() {
        // Buyback {1}{U} (You may pay an additional {1}{U} as you cast this spell. If you do, put
        // this card into your hand as it resolves.)
        addEffect(EffectSlot.STATIC, new BuybackEffect("{1}{U}"));
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));

        // When you cast this spell, draw a card.
        addEffect(EffectSlot.ON_SELF_CAST, new DrawCardEffect());
    }
}
