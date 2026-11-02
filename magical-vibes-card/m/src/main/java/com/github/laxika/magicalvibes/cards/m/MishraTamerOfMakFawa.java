package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToArtifactCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "217")
public class MishraTamerOfMakFawa extends Card {

    public MishraTamerOfMakFawa() {
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessSacrificesEffect()),
                GrantScope.OWN_PERMANENTS));

        ActivatedAbility unearth = new ActivatedAbility(
                false,
                "{1}{B}{R}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .grantHaste(true)
                        .exileAtEndStep(true)
                        .exileIfLeavesBattlefield(true)
                        .unearth(true)
                        .build()),
                "Unearth {1}{B}{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        );

        addStaticEffect(new GrantGraveyardAbilityToArtifactCardsEffect(unearth));
    }
}
