package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;

@CardRegistration(set = "M13", collectorNumber = "124")
@CardRegistration(set = "ORI", collectorNumber = "136")
public class ChandrasFury extends Card {

    public ChandrasFury() {
        // Chandra's Fury deals 4 damage to target player or planeswalker and 1 damage to each
        // creature that player or that planeswalker's controller controls.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerOrPlaneswalkerEffect(4));
        addEffect(EffectSlot.SPELL, new DealDamageToPermanentsTargetControlsEffect(1));
    }
}
