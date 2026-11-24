package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.MakeTargetAttackingCreatureBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "NEM", collectorNumber = "104")
public class FogPatch extends Card {

    public FogPatch() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.DECLARE_BLOCKERS);
        addSpellEffect(new MakeTargetAttackingCreatureBlockedEffect(
                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentIsAttackingPredicate())));
    }
}
