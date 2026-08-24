package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "TSP", collectorNumber = "130")
public class SkulkingKnight extends Card {

    public SkulkingKnight() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY, new SacrificeSelfEffect());
    }
}
