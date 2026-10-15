package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "204")
public class RoaringEarth extends Card {

    public RoaringEarth() {
        PermanentPredicate creatureOrVehicle = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        PermanentPredicate creatureOrVehicleYouControl = new PermanentAllOfPredicate(List.of(
                creatureOrVehicle,
                new PermanentControlledBySourceControllerPredicate()));

        target(new PermanentPredicateTargetFilter(
                creatureOrVehicleYouControl,
                "Target must be a creature or Vehicle you control"))
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{G}{G}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()),
                        new AnimatePermanentsEffect(
                                0, 0,
                                List.of(CardSubtype.SPIRIT),
                                Set.of(Keyword.HASTE),
                                CardColor.GREEN, Set.of(),
                                GrantScope.TARGET, EffectDuration.PERMANENT)),
                "Channel — {X}{G}{G}, Discard this card: Put X +1/+1 counters on target land you control. "
                        + "It becomes a 0/0 green Spirit creature with haste. It's still a land.",
                TargetFilters.landYouControl()));
    }
}
