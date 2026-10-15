package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromTargetSpellEffect;

@CardRegistration(set = "DST", collectorNumber = "4")
public class Hallow extends Card {

    public Hallow() {
        addEffect(EffectSlot.SPELL, PreventDamageFromTargetSpellEffect.withLifeGain());
    }
}
