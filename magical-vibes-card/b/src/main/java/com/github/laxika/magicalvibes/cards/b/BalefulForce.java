package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VMA", collectorNumber = "105")
@CardRegistration(set = "AA4", collectorNumber = "11")
@CardRegistration(set = "C13", collectorNumber = "70")
public class BalefulForce extends Card {

    public BalefulForce() {
        // At the beginning of each upkeep, you draw a card and you lose 1 life.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
    }
}
