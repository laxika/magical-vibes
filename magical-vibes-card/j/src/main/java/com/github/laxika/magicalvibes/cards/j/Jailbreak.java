package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnedPermanentReturnsTargetPermanentCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "NCC", collectorNumber = "17")
@CardRegistration(set = "NCC", collectorNumber = "118")
public class Jailbreak extends Card {

    public Jailbreak() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardIsPermanentPredicate())
                .source(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                .targetGraveyard(true)
                .underOwnersControl(true)
                .build());
        target(new GraveyardCardPredicateTargetFilter(
                new CardIsPermanentPredicate(), GraveyardSearchScope.OPPONENT_GRAVEYARD));
        addEffect(EffectSlot.SPELL, new ReturnedPermanentReturnsTargetPermanentCardFromGraveyardEffect());
    }
}
