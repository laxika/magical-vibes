package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.MakeTargetAttackingCreatureBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "8")
public class DazzlingBeauty extends Card {

    public DazzlingBeauty() {
        // Cast this spell only during the declare blockers step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_BLOCKERS);

        // Target unblocked attacking creature becomes blocked.
        target(TargetFilters.unblockedAttackingCreature())
                .addEffect(EffectSlot.SPELL, new MakeTargetAttackingCreatureBlockedEffect());

        // Draw a card at the beginning of the next turn's upkeep.
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
