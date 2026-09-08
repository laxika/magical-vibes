package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "MSH", collectorNumber = "214")
public class GhostSpectralSaboteur extends Card {

    public GhostSpectralSaboteur() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
