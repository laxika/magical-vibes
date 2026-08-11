package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ODY", collectorNumber = "201")
public class LiquidFire extends Card {

    public LiquidFire() {
        addEffect(EffectSlot.SPELL, new ChooseXValueCost(0, 5));
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature."))
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new XValue()))
                .addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(
                        new Sum(new Fixed(5), new Scaled(new XValue(), -1)),
                        DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
