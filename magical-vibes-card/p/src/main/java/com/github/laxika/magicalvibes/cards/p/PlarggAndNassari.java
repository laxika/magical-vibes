package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MAT", collectorNumber = "18")
@CardRegistration(set = "MAT", collectorNumber = "68")
@CardRegistration(set = "MAT", collectorNumber = "118")
@CardRegistration(set = "MAT", collectorNumber = "160")
@CardRegistration(set = "MAT", collectorNumber = "202")
@CardRegistration(set = "SOC", collectorNumber = "250")
public class PlarggAndNassari extends Card {

    public PlarggAndNassari() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect(2, true));
    }
}
