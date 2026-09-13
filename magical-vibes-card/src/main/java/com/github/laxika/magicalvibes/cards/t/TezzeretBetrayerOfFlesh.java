package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EmblemArtifactTapTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.filter.PermanentActivatedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "84")
public class TezzeretBetrayerOfFlesh extends Card {

    private static final String EMBLEM_TEXT =
            "Whenever an artifact you control becomes tapped, draw a card.";

    public TezzeretBetrayerOfFlesh() {
        addEffect(EffectSlot.STATIC, new ReduceActivatedAbilityCostEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentNotPredicate(new PermanentActivatedThisTurnPredicate()))),
                2));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new DrawCardEffect(2),
                        new DiscardTwoUnlessCardTypeEffect(Set.of(CardType.ARTIFACT))),
                "+1: Draw two cards. Then discard two cards unless you discard an artifact card."
        ));

        AnimatePermanentsEffect animateArtifact = new AnimatePermanentsEffect(
                null, null, List.of(), Set.of(), null, Set.of(CardType.CREATURE),
                GrantScope.TARGET, EffectDuration.PERMANENT, null);
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        animateArtifact,
                        new ConditionalEffect(
                                new NotCondition(new TargetPermanentMatches(
                                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))),
                                new AnimatePermanentsEffect(
                                        4, 4, List.of(), Set.of(), null, Set.of(),
                                        GrantScope.TARGET, EffectDuration.PERMANENT))),
                "−2: Target artifact becomes an artifact creature. If it isn't a Vehicle, it has base power and toughness 4/4.",
                TargetFilters.artifact()
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemArtifactTapTriggerEffect()), EMBLEM_TEXT)),
                "−6: You get an emblem with \"" + EMBLEM_TEXT + "\""
        ));
    }
}
