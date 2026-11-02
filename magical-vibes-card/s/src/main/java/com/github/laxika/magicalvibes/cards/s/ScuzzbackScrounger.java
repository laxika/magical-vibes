package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "153")
@CardRegistration(set = "ECL", collectorNumber = "320")
public class ScuzzbackScrounger extends Card {

    public ScuzzbackScrounger() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new MayEffect(new BlightEffect(1, CreateTokenEffect.ofTreasureToken(1)), "Blight 1?"));
    }
}
