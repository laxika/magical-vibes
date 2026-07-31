package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;

@CardRegistration(set = "ALL", collectorNumber = "18")
public class SustainingSpirit extends Card {

    public SustainingSpirit() {
        // Cumulative upkeep {1}{W}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}{W}"));

        // Damage that would reduce your life total to less than 1 reduces it to 1 instead.
        // Unconditional, unlike Worship — the floor holds even if this stops being a creature.
        addEffect(EffectSlot.STATIC, new DamageLifeFloorEffect(1, LifeFloorCondition.ALWAYS));
    }
}
