package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "EXO", collectorNumber = "16")
public class ReapingTheRewards extends Card {

    public ReapingTheRewards() {
        addEffect(EffectSlot.STATIC, new BuybackEffect(new PermanentIsLandPredicate(), "a land"));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
