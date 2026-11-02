package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "16")
@CardRegistration(set = "MIC", collectorNumber = "54")
public class ShadowKin extends Card {

    public ShadowKin() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect());
    }
}
