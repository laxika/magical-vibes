package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCardsFromHandCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "28")
public class MarchOfOtherworldlyLight extends Card {

    public MarchOfOtherworldlyLight() {
        addEffect(EffectSlot.SPELL, new ExileAnyNumberOfCardsFromHandCost(
                new CardColorPredicate(CardColor.WHITE), 2));
        PermanentPredicate artifactCreatureOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentIsEnchantmentPredicate()));
        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                artifactCreatureOrEnchantment,
                new PermanentMaxManaValueXPredicate()));
        target(new PermanentPredicateTargetFilter(
                targetFilter,
                "Target must be an artifact, creature, or enchantment with mana value X or less"))
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect(targetFilter));
    }
}
