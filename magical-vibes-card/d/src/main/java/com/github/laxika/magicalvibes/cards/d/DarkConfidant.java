package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "94")
@CardRegistration(set = "FIN", collectorNumber = "334")
@CardRegistration(set = "RAV", collectorNumber = "81")
@CardRegistration(set = "MM2", collectorNumber = "75")
@CardRegistration(set = "MMA", collectorNumber = "75")
@CardRegistration(set = "2XM", collectorNumber = "81")
@CardRegistration(set = "2XM", collectorNumber = "342")
@CardRegistration(set = "RVR", collectorNumber = "71")
public class DarkConfidant extends Card {

    public DarkConfidant() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new RevealTopCardPutIntoHandAndChangeLifeEffect(false));
    }
}
