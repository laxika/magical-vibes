package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardIfAllCreaturesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "248")
public class GamePreserve extends Card {

    public GamePreserve() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new EachPlayerRevealsTopCardIfAllCreaturesToBattlefieldEffect());
    }
}
