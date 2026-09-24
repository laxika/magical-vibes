package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;

@CardRegistration(set = "CHK", collectorNumber = "178")
@CardRegistration(set = "MMA", collectorNumber = "121")
@CardRegistration(set = "SLD", collectorNumber = "997")
@CardRegistration(set = "SLD", collectorNumber = "2049")
@CardRegistration(set = "SLD", collectorNumber = "2054")
public class LavaSpike extends Card {

    public LavaSpike() {
        // Lava Spike deals 3 damage to target player or planeswalker.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerOrPlaneswalkerEffect(3));
    }
}
