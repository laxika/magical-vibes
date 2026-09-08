package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STH", collectorNumber = "123")
@CardRegistration(set = "TPR", collectorNumber = "203")
public class VerdantTouch extends Card {

    public VerdantTouch() {
        addEffect(EffectSlot.STATIC, new BuybackEffect("{3}"));
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new AnimatePermanentsEffect(
                2, 2,
                List.of(), Set.of(),
                null, Set.of(),
                GrantScope.TARGET, EffectDuration.PERMANENT));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
