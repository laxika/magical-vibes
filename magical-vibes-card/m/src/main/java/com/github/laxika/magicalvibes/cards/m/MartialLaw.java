package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "14")
public class MartialLaw extends Card {

    public MartialLaw() {
        // At the beginning of your upkeep, detain target creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), new LockTargetPermanentEffect(
                        true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN));
    }
}
