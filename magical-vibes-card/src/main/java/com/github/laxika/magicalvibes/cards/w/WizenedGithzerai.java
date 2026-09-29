package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCombatOpponentEffect;

@CardRegistration(set = "HBG", collectorNumber = "42")
public class WizenedGithzerai extends Card {

    public WizenedGithzerai() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new PerpetuallyBoostCombatOpponentEffect(-2, 0), TriggerMode.PER_BLOCKER);
    }
}
