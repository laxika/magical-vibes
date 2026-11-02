package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.DelayedEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToFrontFaceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Back face of Slicer, Hired Muscle. */
public class SlicerHighSpeedAntagonist extends Card {

    public SlicerHighSpeedAntagonist() {
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new NotControllerTurn()),
                new GrantCardTypeEffect(CardType.CREATURE, GrantScope.SELF)));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DelayedEndOfCombatEffect(new TransformToFrontFaceEffect()));
    }
}
