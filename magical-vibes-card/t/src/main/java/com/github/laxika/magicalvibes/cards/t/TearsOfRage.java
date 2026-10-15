package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOwnCreaturesAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "DST", collectorNumber = "70")
public class TearsOfRage extends Card {

    public TearsOfRage() {
        // Cast this spell only during the declare attackers step.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_ATTACKERS);

        // Attacking creatures you control get +X/+0 until end of turn, where X is the number of
        // attacking creatures.
        PermanentIsAttackingPredicate attacking = new PermanentIsAttackingPredicate();
        PermanentCount attackingCreatures = new PermanentCount(attacking, CountScope.ANY_PLAYER);
        addEffect(EffectSlot.SPELL,
                new BoostAllOwnCreaturesEffect(attackingCreatures, new Fixed(0), attacking));

        // Sacrifice those creatures at the beginning of the next end step.
        addEffect(EffectSlot.SPELL, new SacrificeOwnCreaturesAtEndStepEffect(attacking));
    }
}
