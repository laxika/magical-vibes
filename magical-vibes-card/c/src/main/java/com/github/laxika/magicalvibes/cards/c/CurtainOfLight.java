package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetAttackingCreatureBlockedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOK", collectorNumber = "6")
public class CurtainOfLight extends Card {

    public CurtainOfLight() {
        // Cast this spell only during combat after blockers are declared.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.COMBAT_AFTER_BLOCKERS);

        // Target unblocked attacking creature becomes blocked.
        target(TargetFilters.unblockedAttackingCreature())
                .addEffect(EffectSlot.SPELL, new MakeTargetAttackingCreatureBlockedEffect());

        // Draw a card.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
