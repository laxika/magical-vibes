package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksOneOfYourOpponents;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2197")
public class EllieBrickMaster extends Card {

    public EllieBrickMaster() {
        CreateTokenEffect cordycepsInfected = new CreateTokenEffect(
                1, "Cordyceps Infected", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.FUNGUS, CardSubtype.ZOMBIE), true);
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new PlayerAttacksOneOfYourOpponents(),
                        new CreateTokenForTriggeringPlayerEffect(cordycepsInfected, true)));
    }
}
