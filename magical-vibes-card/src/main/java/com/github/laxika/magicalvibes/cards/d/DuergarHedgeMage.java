package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "EVE", collectorNumber = "137")
public class DuergarHedgeMage extends Card {

    public DuergarHedgeMage() {
        // When this creature enters, if you control two or more Mountains, you may destroy target
        // artifact. Intervening-if gate (CR 603.4): checked as the trigger goes on the stack and again
        // at resolution. The gate defers targeting to trigger time (group 0, target artifact); the
        // "you may" only decides whether to destroy it.
        target(new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(),
                "Target must be an artifact"), 1, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                        new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target artifact?")));

        // When this creature enters, if you control two or more Plains, you may destroy target
        // enchantment. Independent intervening-if gate; targets an enchantment (group 1). The two gates
        // fire independently — the ETB multi-target pipeline skips whichever group's effect was gated out.
        target(new PermanentPredicateTargetFilter(new PermanentIsEnchantmentPredicate(),
                "Target must be an enchantment"), 1, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                        new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target enchantment?")));
    }
}
