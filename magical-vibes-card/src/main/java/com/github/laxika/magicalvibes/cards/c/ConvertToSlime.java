package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfDestroyedPermanents;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "37")
@CardRegistration(set = "DSC", collectorNumber = "64")
public class ConvertToSlime extends Card {

    public ConvertToSlime() {
        var artifactCreatureOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentIsEnchantmentPredicate()));
        setAllowSharedTargets(true);
        target(new PermanentPredicateTargetFilter(
                artifactCreatureOrEnchantment,
                "Target must be an artifact, creature, or enchantment"), 0, 3)
                .addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new Delirium(),
                        new CreateTokenEffect("Ooze", new TotalManaValueOfDestroyedPermanents(),
                                new TotalManaValueOfDestroyedPermanents(), CardColor.GREEN,
                                List.of(CardSubtype.OOZE), Set.of(), Set.of())));
        setMultiTargetConstraint(
                MultiTargetConstraint.AT_MOST_ONE_ARTIFACT_ONE_CREATURE_ONE_ENCHANTMENT_AND_ONE_PLANESWALKER);
    }
}
