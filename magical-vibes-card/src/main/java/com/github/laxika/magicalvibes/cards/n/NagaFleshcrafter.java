package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "52")
public class NagaFleshcrafter extends Card {

    private static final PermanentPredicate NONLEGENDARY_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))
    ));

    private static final PermanentPredicate CREATURES_YOU_CONTROL = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentControlledBySourceControllerPredicate()
    ));

    public NagaFleshcrafter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature"
        ));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, NONLEGENDARY_CREATURE),
                        new EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect(
                                NONLEGENDARY_CREATURE, CREATURES_YOU_CONTROL)
                ),
                "Renew — {2}{U}, Exile this card from your graveyard: Put a +1/+1 counter on target "
                        + "nonlegendary creature you control. Each other creature you control becomes a copy "
                        + "of that creature until end of turn. Activate only as a sorcery.",
                new ControlledPermanentPredicateTargetFilter(
                        NONLEGENDARY_CREATURE,
                        "Target must be a nonlegendary creature you control."),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
