package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "8")
@CardRegistration(set = "CMD", collectorNumber = "16")
public class JTunGrunt extends Card {

    public JTunGrunt() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                CumulativeUpkeepEffect.putCardsFromSingleGraveyard(2));
    }
}
