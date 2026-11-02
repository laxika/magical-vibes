package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C18", collectorNumber = "9")
@CardRegistration(set = "SCD", collectorNumber = "50")
public class EverWatchingThreshold extends Card {

    public EverWatchingThreshold() {
        addTrigger(Triggers.playerAttacks(),
                new ConditionalEffect(new OpponentAttacksWithAtLeastCreatures(1), new DrawCardEffect()));
    }
}
