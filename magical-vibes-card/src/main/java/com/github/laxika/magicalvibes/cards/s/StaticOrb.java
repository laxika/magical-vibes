package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticOrbEffect;

@CardRegistration(set = "7ED", collectorNumber = "319")
public class StaticOrb extends Card {

    public StaticOrb() {
        // Static: as long as this artifact is untapped, players can't untap more than two
        // permanents during their untap steps. The untap step pauses to let the active player
        // pick up to two of the permanents that would otherwise untap.
        addEffect(EffectSlot.STATIC, new StaticOrbEffect());
    }
}
