package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;

@CardRegistration(set = "ORI", collectorNumber = "84")
public class Willbreaker extends Card {

    public Willbreaker() {
        // Whenever a creature an opponent controls becomes the target of a spell or ability you
        // control, gain control of that creature for as long as you control Willbreaker. The
        // targeted creature is set as the non-targeting targetId on the queued trigger.
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY,
                new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD));
    }
}
