package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardThenTargetPlayerReturnsCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerReturnsCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.SpellTarget;

@CardRegistration(set = "C15", collectorNumber = "39")
public class Skullwinder extends Card {

    public Skullwinder() {
        ReturnCardFromGraveyardEffect targetCardReturn = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .targetGraveyard(true)
                .targetGroup(0)
                .build();
        TargetPlayerReturnsCardFromGraveyardToHandEffect targetPlayerReturn =
                new TargetPlayerReturnsCardFromGraveyardToHandEffect(null);
        ReturnTargetCardThenTargetPlayerReturnsCardFromGraveyardToHandEffect orderedEffect =
                new ReturnTargetCardThenTargetPlayerReturnsCardFromGraveyardToHandEffect(
                        targetCardReturn, targetPlayerReturn);

        SpellTarget graveyardTarget = target(new GraveyardCardPredicateTargetFilter(
                null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        graveyardTarget.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, orderedEffect);

        SpellTarget opponentTarget = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent"));
        registerEffectTargetIndex(orderedEffect, opponentTarget.getIndex());
        registerEffectTargetIndex(targetCardReturn, graveyardTarget.getIndex());
        registerEffectTargetIndex(targetPlayerReturn, opponentTarget.getIndex());
    }
}
