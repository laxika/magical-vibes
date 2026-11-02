package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PutCounterOnCreatureThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "23")
@CardRegistration(set = "FIC", collectorNumber = "137")
public class LordJyscalGuado extends Card {

    public LordJyscalGuado() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new PutCounterOnCreatureThisTurn(),
                CreateTokenEffect.ofClueToken(1)));
    }
}
