package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastThisTurnEffect;

@CardRegistration(set = "EOE", collectorNumber = "165")
public class TerritorialBruntar extends Card {

    public TerritorialBruntar() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new ExileTopUntilNonlandMayCastThisTurnEffect());
    }
}
