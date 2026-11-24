package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/**
 * City of Solitude: players can cast spells and activate abilities only during their own turns.
 */
@CardRegistration(set = "VIS", collectorNumber = "102")
public class CityOfSolitude extends Card {

    public CityOfSolitude() {
        addStaticEffect(OpponentsCantCastOrActivateDuringYourTurnEffect.onlyDuringTheirOwnTurns(Subjects.eachPlayer(),
                PlayerRestriction.activatingAbilities()));
    }
}
