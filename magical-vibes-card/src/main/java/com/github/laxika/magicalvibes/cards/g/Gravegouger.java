package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect;

@CardRegistration(set = "TOR", collectorNumber = "62")
public class Gravegouger extends Card {

    public Gravegouger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileCardsFromGraveyardEffect(2, true, true));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect());
    }
}
