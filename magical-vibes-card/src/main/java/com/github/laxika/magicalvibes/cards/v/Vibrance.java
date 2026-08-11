package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "249")
public class Vibrance extends Card {

    public Vibrance() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{R/G}{R/G}"))));

        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new ColorSpentToCast(ManaColor.RED, 2),
                        new DealDamageToAnyTargetEffect(3)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.GREEN, 2),
                new SearchLibraryEffect(new CardTypePredicate(CardType.LAND))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.GREEN, 2),
                new GainLifeEffect(2)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfIfEvokedEffect());
    }
}
