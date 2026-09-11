package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsPutRandomMatchingOntoBattlefieldRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HOB", collectorNumber = "98")
public class GetawayBarrel extends Card {

    public GetawayBarrel() {
        addEffect(EffectSlot.ON_DEATH,
                new RevealTopCardsPutRandomMatchingOntoBattlefieldRestOnBottomEffect(
                        13, new CardTypePredicate(CardType.CREATURE)));
    }
}
