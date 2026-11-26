package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "KHM", collectorNumber = "128")
public class CrushTheWeak extends Card {

    public CrushTheWeak() {
        addSpellEffect(DealDamageEffect.toEachCreature(2).exilingDamagedCreaturesThatWouldDie());
        addCastingOption(new ForetellCast("{R}"));
    }
}
