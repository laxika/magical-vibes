package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "26")
public class RequisitionRaid extends Card {

    public RequisitionRaid() {
        setAllowSharedTargets(true);

        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{1}", "{1}", "{1}")));

        var playerFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.artifact()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target enchantment",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.enchantment()),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on each creature target player controls",
                        new PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect(),
                        playerFilter)
        )));
    }
}
