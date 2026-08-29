package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect;

@CardRegistration(set = "RIX", collectorNumber = "99")
public class DireFleetDaredevil extends Card {

    public DireFleetDaredevil() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect());
    }
}
