package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "P02", collectorNumber = "29")
@CardRegistration(set = "PTK", collectorNumber = "31")
public class WarriorsStand extends Card {

    public WarriorsStand() {
        // Cast only during the declare attackers step and only if you've been attacked this step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_ATTACKERS_IF_ATTACKED);

        // Creatures you control get +2/+2 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 2));
    }
}
