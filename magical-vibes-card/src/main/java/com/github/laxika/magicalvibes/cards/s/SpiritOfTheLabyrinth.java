package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayersCanDrawOnlyOneCardEachTurnEffect;

@CardRegistration(set = "BNG", collectorNumber = "27")
public class SpiritOfTheLabyrinth extends Card {

    public SpiritOfTheLabyrinth() {
        addEffect(EffectSlot.STATIC, new PlayersCanDrawOnlyOneCardEachTurnEffect());
    }
}
