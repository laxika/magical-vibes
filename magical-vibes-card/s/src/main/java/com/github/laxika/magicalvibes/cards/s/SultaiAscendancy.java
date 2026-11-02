package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "203")
@CardRegistration(set = "CP2", collectorNumber = "3")
public class SultaiAscendancy extends Card {

    public SultaiAscendancy() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SurveilEffect(2));
    }
}
