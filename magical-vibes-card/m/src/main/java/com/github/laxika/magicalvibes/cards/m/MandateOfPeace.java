package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.EndCombatPhaseEffect;

@CardRegistration(set = "C19", collectorNumber = "4")
public class MandateOfPeace extends Card {

    public MandateOfPeace() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.COMBAT);
        addSpellEffect(PlayerRestrictionEffect.thisTurn(Subjects.eachOpponent(),
                PlayerRestriction.castingSpells()));
        addSpellEffect(new EndCombatPhaseEffect());
    }
}
