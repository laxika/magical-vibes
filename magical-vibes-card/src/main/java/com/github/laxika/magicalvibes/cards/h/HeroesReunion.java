package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

@CardRegistration(set = "RTR", collectorNumber = "168")
@CardRegistration(set = "INV", collectorNumber = "250")
public class HeroesReunion extends Card {

    public HeroesReunion() {
        // Target player gains 7 life.
        addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(7));
    }
}
