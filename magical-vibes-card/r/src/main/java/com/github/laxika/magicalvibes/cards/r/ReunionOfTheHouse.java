package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "TDC", collectorNumber = "15")
@CardRegistration(set = "TDC", collectorNumber = "55")
public class ReunionOfTheHouse extends Card {

    public ReunionOfTheHouse() {
        target(new GraveyardCardPredicateTargetFilter(
                        new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 99)
                .addSpellEffect(new ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect(10));
        addSpellEffect(new ExileSpellEffect());
    }
}
