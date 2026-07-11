package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;

@CardRegistration(set = "SOS", collectorNumber = "153")
public class LumaretsFavor extends Card {

    public LumaretsFavor() {
        // Infusion — When you cast this spell, copy it if you gained life this turn. You may choose
        // new targets for the copy.
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfConditionEffect(new GainedLifeThisTurn()));

        // Target creature gets +2/+4 until end of turn. (Targeting auto-derived.)
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 4));
    }
}
