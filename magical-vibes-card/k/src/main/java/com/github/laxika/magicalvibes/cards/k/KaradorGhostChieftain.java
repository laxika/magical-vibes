package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromGraveyardOncePerYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SLD", collectorNumber = "1646")
@CardRegistration(set = "2X2", collectorNumber = "238")
@CardRegistration(set = "CMD", collectorNumber = "207")
@CardRegistration(set = "CMM", collectorNumber = "342")
@CardRegistration(set = "CMM", collectorNumber = "581")
public class KaradorGhostChieftain extends Card {

    public KaradorGhostChieftain() {
        // This spell costs {1} less to cast for each creature card in your graveyard. A Karador cast from
        // the graveyard is already on the stack when its cost is determined, so it doesn't count itself.
        addStaticEffect(new ReduceOwnCastCostEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER, true)));

        // Once during each of your turns, you may cast a creature spell from your graveyard.
        addStaticEffect(new CastSpellFromGraveyardOncePerYourTurnEffect(
                new CardTypePredicate(CardType.CREATURE)));
    }
}
