package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ODY", collectorNumber = "201")
public class LiquidFire extends Card {

    public LiquidFire() {
        addCastingCost(new ChooseXValueCost(0, 5));
        DealDamageEffect creatureDamage = DealDamageEffect.toTargetCreature(new XValue());
        DealDamageEffect controllerDamage = DealDamageEffect.to(Subjects.controllerOf(Subjects.target()),
                new Sum(new Fixed(5), new Scaled(new XValue(), -1)));
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature."))
                .bindEffect(creatureDamage)
                .bindEffect(controllerDamage);
        addSimultaneousSpellEffects(creatureDamage, controllerDamage);
    }
}
