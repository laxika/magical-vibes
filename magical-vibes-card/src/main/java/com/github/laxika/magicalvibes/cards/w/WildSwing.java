package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SHM", collectorNumber = "108")
public class WildSwing extends Card {

    public WildSwing() {
        // Choose three target nonenchantment permanents. Destroy one of them at random.
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsEnchantmentPredicate()),
                "Target must be a nonenchantment permanent"
        ), 3, 3).addEffect(EffectSlot.SPELL, new DestroyOneOfTargetsAtRandomEffect());
    }
}
