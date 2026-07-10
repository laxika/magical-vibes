package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "236")
public class SpringCleaning extends Card {

    public SpringCleaning() {
        // Destroy target enchantment.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(),
                "Target must be an enchantment"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(false));

        // Clash with an opponent. If you win, destroy all enchantments your opponents control.
        addEffect(EffectSlot.SPELL, new ClashEffect(new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                ))
        )));
    }
}
