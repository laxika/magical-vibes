package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesSycoraxCommanderVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "161")
@CardRegistration(set = "WHO", collectorNumber = "445")
@CardRegistration(set = "WHO", collectorNumber = "766")
@CardRegistration(set = "WHO", collectorNumber = "1036")
public class SycoraxCommander extends Card {

    public SycoraxCommander() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachOpponentFacesSycoraxCommanderVillainousChoiceEffect());
    }
}
