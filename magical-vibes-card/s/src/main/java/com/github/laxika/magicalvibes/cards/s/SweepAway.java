package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "64")
public class SweepAway extends Card {

    public SweepAway() {
        // Return target creature to its owner's hand. If that creature is attacking, you may put it on
        // top of its owner's library instead.
        //
        // If the creature is attacking on resolution and the caster accepts, it is tucked and the trailing
        // bounce finds nothing; declining (or a non-attacking target) falls through to the bounce.
        target(TargetFilters.creature())
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentIsAttackingPredicate()),
                        new MayEffect(new PutTargetOnTopOfLibraryEffect(),
                                "You may put that creature on top of its owner's library instead.")))
                .addSpellEffect(ReturnToHandEffect.target());
    }
}
