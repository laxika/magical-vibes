package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OneOrMoreArtifactOrCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "MOM", collectorNumber = "330")
public class SeerOfStolenSight extends Card {

    public SeerOfStolenSight() {
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_OR_CREATURE_DIES,
                new OneOrMoreArtifactOrCreatureDeathTriggerEffect(new SurveilEffect(1)));
    }
}
