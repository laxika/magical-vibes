package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "GPT", collectorNumber = "1")
public class AbsolverThrull extends Card {

    public AbsolverThrull() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(), "Target must be an enchantment"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature"))
                .addEffect(EffectSlot.ON_DEATH, new HauntEffect());
        target(new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(), "Target must be an enchantment"))
                .addEffect(EffectSlot.ON_HAUNTED_CREATURE_DIES, new DestroyTargetPermanentEffect());
    }
}
