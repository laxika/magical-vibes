package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ECC", collectorNumber = "7")
@CardRegistration(set = "ECC", collectorNumber = "27")
public class AberrantReturn extends Card {

    public AberrantReturn() {
        CardTypePredicate creatureCards = new CardTypePredicate(CardType.CREATURE);

        addEffect(EffectSlot.SPELL, ReturnTargetCardsFromGraveyardToBattlefieldEffect.fromAllGraveyards(
                creatureCards, 3, 1, CounterType.MINUS_ONE_MINUS_ONE, 1));
    }
}
