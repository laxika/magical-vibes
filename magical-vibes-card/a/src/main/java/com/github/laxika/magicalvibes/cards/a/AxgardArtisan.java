package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "332")
public class AxgardArtisan extends Card {

    public AxgardArtisan() {
        addTrigger(Triggers.plusOneCountersPutOnThis(),
                new OncePerTurnTriggerEffect(CreateTokenEffect.ofTreasureToken(1)));
    }
}
