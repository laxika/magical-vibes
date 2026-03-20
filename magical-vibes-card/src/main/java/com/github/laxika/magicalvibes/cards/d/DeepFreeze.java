package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessStaticEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "50")
public class DeepFreeze extends Card {

    public DeepFreeze() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
        // Enchanted creature has base power and toughness 0/4
        .addEffect(EffectSlot.STATIC, new SetBasePowerToughnessStaticEffect(0, 4, GrantScope.ENCHANTED_CREATURE))
        // Enchanted creature has defender
        .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DEFENDER, GrantScope.ENCHANTED_CREATURE))
        // Enchanted creature loses all other abilities
        .addEffect(EffectSlot.STATIC, new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
        // Enchanted creature is a blue Wall in addition to its other colors and types
        .addEffect(EffectSlot.STATIC, new GrantColorEffect(CardColor.BLUE, GrantScope.ENCHANTED_CREATURE))
        .addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.WALL, GrantScope.ENCHANTED_CREATURE));
    }
}
