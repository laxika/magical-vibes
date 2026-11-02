package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureDarigaazShivanChampionSpellbookEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "22")
public class DarigaazShivanChampion extends Card {

    public DarigaazShivanChampion() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConjureDarigaazShivanChampionSpellbookEffect());
    }
}
