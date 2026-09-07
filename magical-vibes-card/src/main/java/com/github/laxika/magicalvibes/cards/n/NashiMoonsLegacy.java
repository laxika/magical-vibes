package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "39")
public class NashiMoonsLegacy extends Card {

    private static final CardAnyOfPredicate LEGENDARY_OR_RAT = new CardAnyOfPredicate(List.of(
            new CardSupertypePredicate(CardSupertype.LEGENDARY),
            new CardSubtypePredicate(CardSubtype.RAT)));

    public NashiMoonsLegacy() {
        GraveyardSearchScope ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
        target(new GraveyardCardPredicateTargetFilter(LEGENDARY_OR_RAT, ownGraveyard), 0, 1);
        addEffect(EffectSlot.ON_ATTACK,
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        LEGENDARY_OR_RAT, ownGraveyard, 0));
    }
}
