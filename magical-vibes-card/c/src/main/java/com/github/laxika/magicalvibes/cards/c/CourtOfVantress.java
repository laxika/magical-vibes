package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreaturePermanentlyEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WOC", collectorNumber = "22")
@CardRegistration(set = "WOC", collectorNumber = "30")
public class CourtOfVantress extends Card {

    public CourtOfVantress() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());

        PermanentPredicate otherArtifactOrEnchantment = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        PermanentPredicate artifactOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate()));
        var copyEffect = BecomeCopyOfTargetCreaturePermanentlyEffect.forTargetPermanent(
                EffectSlot.UPKEEP_TRIGGERED, artifactOrEnchantment);

        target(new PermanentPredicateTargetFilter(
                        otherArtifactOrEnchantment,
                        "Target must be another artifact or enchantment"), 0, 1)
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                                new ConditionalReplacementEffect(
                                        new ControllerIsMonarch(),
                                        copyEffect,
                                        new CreateTokenCopyOfTargetPermanentEffect()),
                        "Use Court of Vantress's upkeep ability?"));
    }
}
