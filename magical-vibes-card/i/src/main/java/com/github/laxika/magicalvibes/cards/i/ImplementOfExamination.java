package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "156")
@CardRegistration(set = "KLR", collectorNumber = "244")
public class ImplementOfExamination extends Card {

    public ImplementOfExamination() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{U}, Sacrifice this artifact: Draw a card."
        ));
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS), new DrawCardEffect(1));
    }
}
