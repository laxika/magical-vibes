package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "23")
public class Cryptoplasm extends Card {

    public Cryptoplasm() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new BecomeCopyOfTargetCreatureEffect());
    }
}
