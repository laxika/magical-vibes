package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "129")
public class ZombieOgre extends Card {

    public ZombieOgre() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Morbid()),
                new VentureIntoDungeonEffect());
    }
}
