package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "219")
public class MalcolmTheEyes extends Card {

    public MalcolmTheEyes() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), CreateTokenEffect.ofClueToken(1));
    }
}
