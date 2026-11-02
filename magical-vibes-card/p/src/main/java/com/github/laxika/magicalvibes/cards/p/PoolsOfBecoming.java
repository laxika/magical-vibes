package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutHandOnBottomOfLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopPlanarCardsAndTriggerChaosEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "30")
@CardRegistration(set = "OPCA", collectorNumber = "63")
public class PoolsOfBecoming extends Card {

    public PoolsOfBecoming() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new PutHandOnBottomOfLibraryAndDrawEffect());
        addTrigger(Triggers.chaosEnsues(), new RevealTopPlanarCardsAndTriggerChaosEffect(3));
    }
}
