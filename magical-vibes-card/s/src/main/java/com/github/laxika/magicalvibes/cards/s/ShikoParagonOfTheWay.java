package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "223")
public class ShikoParagonOfTheWay extends Card {

    public ShikoParagonOfTheWay() {
        CardPredicate nonlandManaValueAtMostThree = new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                new CardMaxManaValuePredicate(3)));
        GraveyardSearchScope ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;

        target(new GraveyardCardPredicateTargetFilter(nonlandManaValueAtMostThree, ownGraveyard));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(nonlandManaValueAtMostThree, ownGraveyard));
    }
}
