package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PlayerWithLowestLifeGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "13")
public class LoxodonPeacekeeper extends Card {

    public LoxodonPeacekeeper() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PlayerWithLowestLifeGainsControlOfSourceCreatureEffect());
    }
}
