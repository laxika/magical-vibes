package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.CamouflageEffect;

@CardRegistration(set = "2ED", collectorNumber = "188")
public class Camouflage extends Card {

    public Camouflage() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_ATTACKERS);
        addEffect(EffectSlot.SPELL, new CamouflageEffect());
    }
}
