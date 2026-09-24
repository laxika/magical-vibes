package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "183")
public class TearAsunder extends Card {

    public TearAsunder() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{B}"));

        PermanentPredicate artifactOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate()));
        PermanentPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        target(new PermanentPredicateTargetFilter(
                artifactOrEnchantment,
                "Target must be an artifact or enchantment",
                nonland
        )).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(),
                new ExileTargetPermanentEffect(),
                new ExileTargetPermanentEffect()));
    }
}
