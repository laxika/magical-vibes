package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

import static com.github.laxika.magicalvibes.model.filter.PlayerRelation.ANY;

@CardRegistration(set = "MRD", collectorNumber = "95")
public class GrabTheReins extends Card {

    public GrabTheReins() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}{R}"));
        setAllowSharedTargets(true);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, you gain control of target creature and it gains haste",
                        List.of(
                                new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Sacrifice a creature. Grab the Reins deals damage equal to that creature's power to any target",
                        new SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(),
                        new AnyTargetPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate())),
                                new PlayerRelationPredicate(ANY),
                                "Target must be any target"))
        )));
    }
}
