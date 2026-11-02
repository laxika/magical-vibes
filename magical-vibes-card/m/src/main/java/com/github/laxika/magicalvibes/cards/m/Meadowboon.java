package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "17")
@CardRegistration(set = "MMA", collectorNumber = "22")
public class Meadowboon extends Card {

    public Meadowboon() {
        // "When this creature leaves the battlefield, put a +1/+1 counter on each creature target player controls."
        // The target player is chosen at leaves-the-battlefield time via the its leaves-the-battlefield trigger
        // pipeline — no cast-time target filter (that would wrongly prompt when the creature is cast).
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect());

        // Evoke {3}{W}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{W}"))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeSelfIfEvokedEffect());
    }
}
