package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeTargetLegendaryCreatureCopyOfMemoryCounterExiledCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetLegendaryCreatureCardFromGraveyardWithMemoryCounterEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ExiledCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "69")
public class TheAnimus extends Card {

    public TheAnimus() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ExileTargetLegendaryCreatureCardFromGraveyardWithMemoryCounterEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BecomeTargetLegendaryCreatureCopyOfMemoryCounterExiledCreatureUntilNextTurnEffect()),
                "{T}: Until your next turn, target legendary creature you control becomes a copy of target creature card in exile with a memory counter on it. Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED,
                List.of(
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
                                "Target must be a legendary creature you control."),
                        new ExiledCardPredicateTargetFilter(
                                new CardTypePredicate(CardType.CREATURE),
                                "Target must be a creature card in exile with a memory counter on it.")
                ),
                2,
                2));
    }
}
