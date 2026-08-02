package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "TMP", collectorNumber = "134")
public class EvincarsJustice extends Card {

    public EvincarsJustice() {
        // Buyback {3} (You may pay an additional {3} as you cast this spell. If you do, put this
        // card into your hand as it resolves.)
        // Evincar's Justice deals 2 damage to each creature and each player.
        addEffect(EffectSlot.STATIC, new BuybackEffect("{3}"));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(2, true));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
