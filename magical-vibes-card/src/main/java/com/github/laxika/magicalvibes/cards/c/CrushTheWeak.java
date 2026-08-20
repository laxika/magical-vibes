package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "KHM", collectorNumber = "128")
public class CrushTheWeak extends Card {

    public CrushTheWeak() {
        addEffect(EffectSlot.SPELL, MassDamageEffect.exilingDamageToEachCreature(2));
        addCastingOption(new ForetellCast("{R}"));
    }
}
