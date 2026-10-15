package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageToSourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKC", collectorNumber = "12")
@CardRegistration(set = "MKC", collectorNumber = "323")
public class OtherworldlyEscort extends Card {

    public OtherworldlyEscort() {
        addEffect(EffectSlot.ON_DEATH, new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)),
                new ReturnDyingCreatureToOwnerBattlefieldEffect(
                        CounterType.CHARGE, 4, null, Set.of(), false, false,
                        List.of(CardSubtype.SPIRIT, CardSubtype.DETECTIVE))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CHARGE),
                        new DestroyTargetPermanentEffect()),
                "{1}{W}, {T}, Remove a charge counter from this creature: Destroy target creature that dealt damage to you this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentDealtDamageToSourceControllerThisTurnPredicate()
                        )),
                        "Target must be a creature that dealt damage to you this turn")));
    }
}
