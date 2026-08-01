package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayersCanCastAndActivateOnlyDuringOwnTurnEffect;

/**
 * City of Solitude: players can cast spells and activate abilities only during their own turns.
 */
@CardRegistration(set = "VIS", collectorNumber = "102")
public class CityOfSolitude extends Card {

    public CityOfSolitude() {
        addEffect(EffectSlot.STATIC, new PlayersCanCastAndActivateOnlyDuringOwnTurnEffect());
    }
}
