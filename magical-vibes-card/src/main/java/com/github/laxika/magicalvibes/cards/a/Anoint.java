package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "TMP", collectorNumber = "3")
public class Anoint extends Card {

    public Anoint() {
        // Buyback {3} (You may pay an additional {3} as you cast this spell. If you do, put this
        // card into your hand as it resolves.)
        // Prevent the next 3 damage that would be dealt to target creature this turn.
        addEffect(EffectSlot.STATIC, new BuybackEffect("{3}"));
        addEffect(EffectSlot.SPELL, PreventDamageEffect.nextToTargetCreature(3));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
