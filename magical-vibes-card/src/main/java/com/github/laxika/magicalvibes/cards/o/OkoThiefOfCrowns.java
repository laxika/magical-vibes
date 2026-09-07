package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentBecomesSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "197")
public class OkoThiefOfCrowns extends Card {

    public OkoThiefOfCrowns() {
        addActivatedAbility(new ActivatedAbility(
                2,
                List.of(foodToken()),
                "+2: Create a Food token."
        ));

        TargetFilter artifactOrCreature = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                "Target must be an artifact or creature");
        addActivatedAbility(new ActivatedAbility(
                1,
                List.of(
                        new SetCardTypesEffect(Set.of(CardType.CREATURE), GrantScope.TARGET),
                        new SetTargetColorEffect(CardColor.GREEN),
                        new TargetPermanentBecomesSubtypeEffect(CardSubtype.ELK),
                        new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.PERMANENT),
                        new SetBasePowerToughnessEffect(3, 3, GrantScope.TARGET, EffectDuration.PERMANENT)
                ),
                "+1: Target artifact or creature loses all abilities and becomes a green Elk creature with base power and toughness 3/3.",
                artifactOrCreature
        ));

        TargetFilter controlledArtifactOrCreature = new ControlledPermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                "Target must be an artifact or creature you control");
        TargetFilter opponentCreaturePowerThreeOrLess = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentPowerAtMostPredicate(3)
                )),
                "Target must be a creature an opponent controls with power 3 or less");
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExchangeControlOfTargetPermanentsEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                                new PermanentPowerAtMostPredicate(3)
                        )),
                        false,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        false
                )),
                "-5: Exchange control of target artifact or creature you control and target creature an opponent controls with power 3 or less.",
                null,
                -5,
                null,
                null,
                List.of(controlledArtifactOrCreature, opponentCreaturePowerThreeOrLess),
                2,
                2
        ));
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )));
    }
}
