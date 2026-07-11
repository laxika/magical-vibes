package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SetEachPlayerLifeToHighestAmongPlayersEffect;

@CardRegistration(set = "LRW", collectorNumber = "2")
public class ArbiterOfKnollridge extends Card {

    public ArbiterOfKnollridge() {
        // When this creature enters, each player's life total becomes the highest life total among all players.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SetEachPlayerLifeToHighestAmongPlayersEffect());
    }
}
