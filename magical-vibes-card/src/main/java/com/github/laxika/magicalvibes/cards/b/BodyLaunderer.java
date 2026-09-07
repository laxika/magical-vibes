package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "68")
public class BodyLaunderer extends Card {

    public BodyLaunderer() {
        var returnFilter = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.ROGUE)),
                new CardNotPredicate(new CardIsSelfPredicate()),
                new CardPowerAtMostSourcePowerPredicate()
        ));

        target(new GraveyardCardPredicateTargetFilter(returnFilter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new DrawDiscardAndConniveEffect());
        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(returnFilter)
                .targetGraveyard(true)
                .build());
    }
}
