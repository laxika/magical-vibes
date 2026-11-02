package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfReturnAtNextUpkeepWithHasteEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSOS", collectorNumber = "29")
public class SunderingSentinel extends Card {

    public SunderingSentinel() {
        setStartingIntensity(2);

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToAnyTargetEffect(new SourceIntensity()));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(new SourceIntensity()));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new IntensifySourceCardEffect(1));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ExileSelfReturnAtNextUpkeepWithHasteEffect());
    }
}
