package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "274")
public class AshiokSculptorOfFears extends Card {

    public AshiokSculptorOfFears() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(
                        new DrawCardEffect(1),
                        new MillEffect(2, MillRecipient.CONTROLLER),
                        new MillEffect(2, MillRecipient.EACH_OPPONENT)),
                "+2: Draw a card. Each player mills two cards."));

        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build()),
                "−5: Put target creature card from a graveyard onto the battlefield under your control."));

        addActivatedAbility(new ActivatedAbility(
                -11,
                List.of(new GainControlOfAllPermanentsTargetPlayerControlsEffect(
                        new PermanentIsCreaturePredicate())),
                "−11: Gain control of all creatures target opponent controls.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
