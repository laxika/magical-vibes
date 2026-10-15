package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "RNA", collectorNumber = "273")
public class TheHauntOfHightower extends Card {

    public TheHauntOfHightower() {
        addEffect(EffectSlot.ON_ATTACK,
                new DiscardEffect(1, DiscardRecipient.DEFENDING_PLAYER));
        addEffect(EffectSlot.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
