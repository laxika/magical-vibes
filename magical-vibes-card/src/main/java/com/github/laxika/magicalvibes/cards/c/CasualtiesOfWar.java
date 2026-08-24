package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "187")
public class CasualtiesOfWar extends Card {

    public CasualtiesOfWar() {
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.artifact()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target enchantment",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.enchantment()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target land",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.land()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target planeswalker",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsPlaneswalkerPredicate(), "Target must be a planeswalker"))
        )));
    }
}
