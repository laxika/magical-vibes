package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "RAV", collectorNumber = "160")
public class DryadsCaress extends Card {

    public DryadsCaress() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER)));

        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.WHITE),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                        new PermanentIsCreaturePredicate())));
    }
}
