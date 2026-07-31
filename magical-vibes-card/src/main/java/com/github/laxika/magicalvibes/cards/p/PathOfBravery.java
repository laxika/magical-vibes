package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;

@CardRegistration(set = "M14", collectorNumber = "26")
public class PathOfBravery extends Card {

    public PathOfBravery() {
        // As long as your life total is greater than or equal to your starting life total, creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerLifeAtLeast(GameData.STARTING_LIFE_TOTAL),
                new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES)));

        // Whenever one or more creatures you control attack, you gain life equal to the number of attacking creatures.
        // The attacker count is snapshotted onto the trigger's xValue when attackers are declared.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new GainLifeEffect(new XValue()));
    }
}
