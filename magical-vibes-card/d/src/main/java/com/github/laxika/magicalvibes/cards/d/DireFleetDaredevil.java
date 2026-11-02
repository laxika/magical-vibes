package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "99")
@CardRegistration(set = "AFC", collectorNumber = "121")
@CardRegistration(set = "OTC", collectorNumber = "162")
@CardRegistration(set = "LCC", collectorNumber = "223")
public class DireFleetDaredevil extends Card {

    public DireFleetDaredevil() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect());
    }
}
