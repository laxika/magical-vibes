package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "81")
@CardRegistration(set = "SLD", collectorNumber = "1431")
@CardRegistration(set = "SPG", collectorNumber = "5")
@CardRegistration(set = "LCC", collectorNumber = "208")
@CardRegistration(set = "PIP", collectorNumber = "187")
@CardRegistration(set = "PIP", collectorNumber = "715")
@CardRegistration(set = "MB2", collectorNumber = "46")
@CardRegistration(set = "FIC", collectorNumber = "281")
public class PitilessPlunderer extends Card {

    public PitilessPlunderer() {
        // Whenever another creature you control dies, create a Treasure token.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), CreateTokenEffect.ofTreasureToken(1));
    }
}
