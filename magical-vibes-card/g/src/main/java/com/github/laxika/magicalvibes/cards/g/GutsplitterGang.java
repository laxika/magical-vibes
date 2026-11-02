package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "106")
public class GutsplitterGang extends Card {

    public GutsplitterGang() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new MayEffect(new BlightEffect(2, null), "Blight 2?", new LoseLifeEffect(3)));
    }
}
