package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "INR", collectorNumber = "66")
public class GeistlightSnare extends Card {

    public GeistlightSnare() {
        // This spell costs {1} less to cast if you control a Spirit.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)),
                new ReduceOwnCastCostEffect(new Fixed(1))));
        // It also costs {1} less to cast if you control an enchantment.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentIsEnchantmentPredicate()),
                new ReduceOwnCastCostEffect(new Fixed(1))));
        // Counter target spell unless its controller pays {3}.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(3));
    }
}
