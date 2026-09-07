package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetAttackingCreatureBlockedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WTH", collectorNumber = "123")
public class ChokingVines extends Card {

    public ChokingVines() {
        // Cast this spell only during the declare blockers step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_BLOCKERS);

        // X target attacking creatures become blocked. Choking Vines deals 1 damage to each of those
        // creatures. One X-scaled attacking-creature group feeds both effects; a creature that is
        // already blocked just takes the damage.
        targetExactlyX(TargetFilters.attackingCreature(), 100)
                .addEffect(EffectSlot.SPELL, new MakeTargetAttackingCreatureBlockedEffect())
                .addEffect(EffectSlot.SPELL, new DealDamageToEachTargetEffect(new Fixed(1)));
    }
}
