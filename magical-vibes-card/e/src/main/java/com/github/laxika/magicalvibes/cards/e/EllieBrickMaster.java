package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2197")
public class EllieBrickMaster extends Card {

    public EllieBrickMaster() {
        CreateTokenEffect cordycepsInfected = new CreateTokenEffect(
                1, "Cordyceps Infected", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.FUNGUS, CardSubtype.ZOMBIE), true);
        addTrigger(Triggers.playerAttacksOneOfYourOpponents(),
                new CreateTokenForTriggeringPlayerEffect(cordycepsInfected, true));
    }
}
