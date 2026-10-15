package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;

@CardRegistration(set = "DSK", collectorNumber = "226")
public class PeerPastTheVeil extends Card {

    public PeerPastTheVeil() {
        addEffect(EffectSlot.SPELL,
                new DiscardOwnHandThenDrawEffect(new CardTypesAmongCardsInGraveyard()));
    }
}
