package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "MH2", collectorNumber = "181")
public class UrbanDaggertooth extends Card {

    public UrbanDaggertooth() {
        // Enrage — Whenever this creature is dealt damage, proliferate.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new ProliferateEffect());
    }
}
