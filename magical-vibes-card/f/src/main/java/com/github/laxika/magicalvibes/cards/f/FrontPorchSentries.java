package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "67")
public class FrontPorchSentries extends Card {

    public FrontPorchSentries() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.dies(ObjectRelation.THIS), BoostEffect.target(-1, -1));
    }
}
