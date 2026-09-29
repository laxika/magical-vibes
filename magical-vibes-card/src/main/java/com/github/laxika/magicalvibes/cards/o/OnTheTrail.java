package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MKC", collectorNumber = "39")
@CardRegistration(set = "MKC", collectorNumber = "349")
public class OnTheTrail extends Card {

    public OnTheTrail() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD, new MayEffect(
                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land", true),
                "Put a land card from your hand onto the battlefield tapped?"
        ));
    }
}
