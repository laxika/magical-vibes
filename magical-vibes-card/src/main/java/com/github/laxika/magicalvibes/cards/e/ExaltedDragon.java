package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessSacrificeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "EXO", collectorNumber = "6")
@CardRegistration(set = "TPR", collectorNumber = "12")
public class ExaltedDragon extends Card {

    public ExaltedDragon() {
        // This creature can't attack unless you sacrifice a land.
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new ControlsPermanentCount(1, new PermanentIsLandPredicate()),
                "you sacrifice a land"));
        addEffect(EffectSlot.STATIC, new CantAttackUnlessSacrificeEffect(
                1,
                new PermanentIsLandPredicate(),
                "a land"));
    }
}
