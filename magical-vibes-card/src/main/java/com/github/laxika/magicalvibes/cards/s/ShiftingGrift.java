package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "66")
public class ShiftingGrift extends Card {

    public ShiftingGrift() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{2}", "{1}", "{1}")));
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                exchangeMode("Exchange control of two target creatures",
                        new PermanentIsCreaturePredicate(), "Targets must be creatures."),
                exchangeMode("Exchange control of two target artifacts",
                        new PermanentIsArtifactPredicate(), "Targets must be artifacts."),
                exchangeMode("Exchange control of two target enchantments",
                        new PermanentIsEnchantmentPredicate(), "Targets must be enchantments.")
        )));
    }

    private static ChooseOneEffect.ChooseOneOption exchangeMode(
            String label, PermanentPredicate predicate, String targetErrorMessage) {
        return new ChooseOneEffect.ChooseOneOption(
                label,
                List.of(ExchangeControlOfTargetPermanentsEffect.forTwoTargetsInOneGroup(predicate)),
                new PermanentPredicateTargetFilter(predicate, targetErrorMessage),
                null,
                2,
                2,
                false,
                null);
    }
}
