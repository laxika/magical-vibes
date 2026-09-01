package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "LEG", collectorNumber = "145")
public class FallingStar extends Card {

    public FallingStar() {
        addEffect(EffectSlot.SPELL, MassDamageEffect.damageAndTapEachCreature(3));
    }
}
