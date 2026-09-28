package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "66")
@CardRegistration(set = "MSC", collectorNumber = "381")
public class ItsClobberinTime extends Card {

    public ItsClobberinTime() {
        TargetFilter artifactOrEnchantment = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                "Target must be an artifact or enchantment");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control deals damage equal to its power to target creature an opponent controls",
                        List.<CardEffect>of(new TargetDealsPowerDamageToTargetEffect()),
                        List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls())),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact or enchantment",
                        new DestroyTargetPermanentEffect(), artifactOrEnchantment)
        )));
    }
}
