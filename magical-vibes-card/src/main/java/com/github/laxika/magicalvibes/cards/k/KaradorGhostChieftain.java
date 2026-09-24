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

@CardRegistration(set = "SLD", collectorNumber = "1646")
public class KaradorGhostChieftain extends Card {

    public KaradorGhostChieftain() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER)));
        addEffect(EffectSlot.STATIC, new CastSpellFromGraveyardOncePerYourTurnEffect(
                new CardTypePredicate(CardType.CREATURE)));
    }
}
