package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.RegisterCombatDamageReflectionEffect;

@CardRegistration(set = "POR", collectorNumber = "18")
public class HarshJustice extends Card {

    public HarshJustice() {
        // Cast only during the declare attackers step and only if you've been attacked this step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_ATTACKERS_IF_ATTACKED);

        // This turn, whenever an attacking creature deals combat damage to you, it deals that much
        // damage to its controller.
        addEffect(EffectSlot.SPELL, new RegisterCombatDamageReflectionEffect());
    }
}
