package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ExtraTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;

@CardRegistration(set = "THS", collectorNumber = "196")
public class MedomaiTheAgeless extends Card {

    public MedomaiTheAgeless() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ControllerExtraTurnEffect(1));
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new NotCondition(new ExtraTurn()),
                "it isn't an extra turn"
        ));
    }
}
