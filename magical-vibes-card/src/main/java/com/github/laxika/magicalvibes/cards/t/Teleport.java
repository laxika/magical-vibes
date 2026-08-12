package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHR", collectorNumber = "26")
public class Teleport extends Card {

    public Teleport() {
        // Cast only during the declare attackers step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_ATTACKERS);

        // Target creature can't be blocked this turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new MakeCreatureUnblockableEffect());
    }
}
