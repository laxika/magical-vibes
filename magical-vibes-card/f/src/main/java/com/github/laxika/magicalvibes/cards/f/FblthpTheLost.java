package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.EnteredFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "50")
@CardRegistration(set = "SLD", collectorNumber = "583")
@CardRegistration(set = "TSR", collectorNumber = "308")
@CardRegistration(set = "RVR", collectorNumber = "44")
public class FblthpTheLost extends Card {

    public FblthpTheLost() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new EnteredFromZone(Zone.LIBRARY, true), new DrawCardEffect()));
        addTrigger(Triggers.becomesTargetOfSpell(), new ShuffleSelfIntoOwnerLibraryEffect());
    }
}
