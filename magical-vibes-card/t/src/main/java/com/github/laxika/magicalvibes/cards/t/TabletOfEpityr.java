package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ATQ", collectorNumber = "67")
@CardRegistration(set = "ME4", collectorNumber = "230")
public class TabletOfEpityr extends Card {

    public TabletOfEpityr() {
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),
                new TriggeringArtifactControllerConditionalEffect(
                        new MayPayManaEffect("{1}", new GainLifeEffect(1), "Pay {1} to gain 1 life?")));
    }
}
