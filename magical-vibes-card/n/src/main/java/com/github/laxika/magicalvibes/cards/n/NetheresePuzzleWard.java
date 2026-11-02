package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD4Effect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFC", collectorNumber = "17")
public class NetheresePuzzleWard extends Card {

    public NetheresePuzzleWard() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RollD4Effect(null, new ScryEffect(new EventValue())));
        addTrigger(Triggers.youRollHighestNaturalResult(),
                new DrawCardEffect());
    }
}
