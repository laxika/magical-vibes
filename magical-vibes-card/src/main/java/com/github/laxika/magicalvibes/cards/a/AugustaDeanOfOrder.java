package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

public class AugustaDeanOfOrder extends Card {

    public AugustaDeanOfOrder() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_TAPPED_CREATURES));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 1, GrantScope.OWN_UNTAPPED_CREATURES));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, SequenceEffect.of(
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, creature),
                new TapAnyNumberPermanentsEffect(creature)));
    }
}
