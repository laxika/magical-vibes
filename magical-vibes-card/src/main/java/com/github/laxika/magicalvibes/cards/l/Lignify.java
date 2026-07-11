package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "228")
public class Lignify extends Card {

    public Lignify() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
        // Enchanted creature has base power and toughness 0/4
        .addEffect(EffectSlot.STATIC, new SetBasePowerToughnessEffect(0, 4, GrantScope.ENCHANTED_CREATURE))
        // Enchanted creature loses all abilities
        .addEffect(EffectSlot.STATIC, new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
        // Enchanted creature is a Treefolk (replaces its other creature types)
        .addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.TREEFOLK, GrantScope.ENCHANTED_CREATURE, true));
    }
}
