package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.EndCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsThisTurnEffect;

@CardRegistration(set = "C19", collectorNumber = "4")
public class MandateOfPeace extends Card {

    public MandateOfPeace() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.COMBAT);
        addEffect(EffectSlot.SPELL, new OpponentsCantCastSpellsThisTurnEffect());
        addEffect(EffectSlot.SPELL, new EndCombatPhaseEffect());
    }
}
