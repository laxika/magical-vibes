package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "53")
@CardRegistration(set = "LCC", collectorNumber = "85")
public class SkeletonCrew extends Card {

    public SkeletonCrew() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SKELETON, CardSubtype.PIRATE))));

        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                new CreateTokenEffect("Skeleton Pirate", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.SKELETON, CardSubtype.PIRATE), Set.of(), Set.of()));

        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{5}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .enterTapped(true)
                        .build()),
                "{5}{B}: Return Skeleton Crew from your graveyard to the battlefield tapped."
        ));
    }
}
