package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "172")
public class AppliedGeometry extends Card {

    public AppliedGeometry() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.AURA)),
                "Target must be a non-Aura permanent you control"
        )).addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect(
                List.of(CardSubtype.FRACTAL),
                Set.of(CardType.CREATURE),
                0,
                0,
                Map.of(CounterType.PLUS_ONE_PLUS_ONE, 6)
        ));
    }
}
