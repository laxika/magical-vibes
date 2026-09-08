package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "44")
public class AberrantMindSorcerer extends Card {

    public AberrantMindSorcerer() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        ReturnCardFromGraveyardEffect returnToTop = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                .filter(instantOrSorcery)
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .targetGraveyard(true)
                .build();
        ReturnCardFromGraveyardEffect returnToHand = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(instantOrSorcery)
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .targetGraveyard(true)
                .build();

        target(new GraveyardCardPredicateTargetFilter(
                instantOrSorcery, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RollD20Effect(
                        new MayEffect(returnToTop, "Put the targeted card on top of your library?"),
                        returnToHand));
    }
}
