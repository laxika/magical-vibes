package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromGraveyardOncePerYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "2X2", collectorNumber = "238")
@CardRegistration(set = "CMD", collectorNumber = "207")
public class KaradorGhostChieftain extends Card {

    public KaradorGhostChieftain() {
        // This spell costs {1} less to cast for each creature card in your graveyard.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER)));

        // Once during each of your turns, you may cast a creature spell from your graveyard.
        addEffect(EffectSlot.STATIC, new CastSpellFromGraveyardOncePerYourTurnEffect(
                new CardTypePredicate(CardType.CREATURE)));
    }
}
