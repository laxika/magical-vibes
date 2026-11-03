package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
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
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "249")
@CardRegistration(set = "ECL", collectorNumber = "295")
public class Vibrance extends Card {

    public Vibrance() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{R/G}{R/G}"))));

        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        TriggerMode.INDEPENDENT,
                        TriggerOptions.DEFAULT.withInterveningIf(new ColorSpentToCast(ManaColor.RED, 2)),
                        new DealDamageToAnyTargetEffect(3));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new ColorSpentToCast(ManaColor.GREEN, 2)),
                new SearchLibraryEffect(new CardTypePredicate(CardType.LAND)));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new ColorSpentToCast(ManaColor.GREEN, 2)),
                new GainLifeEffect(2));

        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeSelfIfEvokedEffect());
    }
}
