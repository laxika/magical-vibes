package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "112")
public class JaxisTheTroublemaker extends Card {

    public JaxisTheTroublemaker() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{R}"))));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastForAlternateCost(), new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastForAlternateCost(), new SacrificeSelfAtEndStepEffect()));
        addEffect(EffectSlot.ON_DEATH, new ConditionalEffect(
                new CastForAlternateCost(), new DrawCardEffect()));

        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        ControlledPermanentPredicateTargetFilter targetFilter = new ControlledPermanentPredicateTargetFilter(
                anotherCreature, "Target must be another creature you control");

        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(), Set.of(), null, null, Map.of(),
                                true, false, true, false,
                                false, false, null, Set.of(),
                                false, Map.of(EffectSlot.ON_DEATH, List.<CardEffect>of(new DrawCardEffect()))
                        )
                ),
                "{R}, {T}, Discard a card: Create a token that's a copy of another target creature you control. "
                        + "It gains haste and \"When this token dies, draw a card.\" Sacrifice it at the beginning "
                        + "of the next end step. Activate only as a sorcery.",
                targetFilter,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
