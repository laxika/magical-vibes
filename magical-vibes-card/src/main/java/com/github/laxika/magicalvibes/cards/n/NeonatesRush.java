package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "INR", collectorNumber = "166")
public class NeonatesRush extends Card {

    public NeonatesRush() {
        // This spell costs {1} less to cast if you control a Vampire.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)),
                new ReduceOwnCastCostEffect(new Fixed(1))));
        // Neonate's Rush deals 1 damage to target creature and 1 damage to its controller. Draw a card.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PERMANENT_CONTROLLER));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
