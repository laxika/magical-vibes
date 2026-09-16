package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardTargetedRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromControlledPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "BOT", collectorNumber = "3")
@CardRegistration(set = "BOT", collectorNumber = "18")
public class JetfireIngeniousScientist extends Card {

    public JetfireIngeniousScientist() {
        setBackFaceCard(new JetfireAirGuardian());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{3}{U}"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveOneOrMoreCountersFromControlledPermanentsCost(
                                CounterType.PLUS_ONE_PLUS_ONE, new PermanentIsArtifactPredicate()),
                        new AwardTargetedRestrictedManaEffect(
                                ManaColor.COLORLESS, new XValue(), new ManaRestriction.Powerstone()),
                        new TransformSelfEffect()
                ),
                "Remove one or more +1/+1 counters from among artifacts you control: Target player adds that much {C}. This mana can't be spent to cast nonartifact spells. Convert Jetfire.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ).withXValue());
    }

    @Override
    public String getBackFaceClassName() {
        return "JetfireAirGuardian";
    }
}
