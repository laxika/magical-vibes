package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "23")
public class WallOfShards extends Card {

    public WallOfShards() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), CumulativeUpkeepEffect.opponentGainsLife(1));
    }
}
