package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "TMP", collectorNumber = "53")
public class WorthyCause extends Card {

    public WorthyCause() {
        // Buyback {2}.
        addEffect(EffectSlot.STATIC, new BuybackEffect("{2}"));
        // Additional cost: sacrifice a creature; its toughness is snapshotted into xValue.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(false, false, true));
        // You gain life equal to the sacrificed creature's toughness.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
