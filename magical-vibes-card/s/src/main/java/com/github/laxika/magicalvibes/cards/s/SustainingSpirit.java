package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "18")
@CardRegistration(set = "ME2", collectorNumber = "36")
public class SustainingSpirit extends Card {

    public SustainingSpirit() {
        // Cumulative upkeep {1}{W}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{1}{W}"));

        // Damage that would reduce your life total to less than 1 reduces it to 1 instead.
        // Unconditional, unlike Worship — the floor holds even if this stops being a creature.
        addStaticEffect(new DamageLifeFloorEffect(1, LifeFloorCondition.ALWAYS));
    }
}
