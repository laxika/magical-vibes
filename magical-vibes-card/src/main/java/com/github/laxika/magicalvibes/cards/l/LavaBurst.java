package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ICE", collectorNumber = "198")
@CardRegistration(set = "DKM", collectorNumber = "16")
public class LavaBurst extends Card {

    public LavaBurst() {
        // Lava Burst deals X damage to any target. If the target is a creature, that damage
        // can't be prevented — the unpreventable gate is the target-dependent
        // TargetPermanentMatches(creature), so damage to a player stays preventable.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(
                new XValue(), new TargetPermanentMatches(new PermanentIsCreaturePredicate()), true));
    }
}
