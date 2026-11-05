package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestToughness;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "119")
public class AbzanBeastmaster extends Card {

    public AbzanBeastmaster() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new ControlsCreatureWithGreatestToughness(),
                new DrawCardEffect(1)));
    }
}
