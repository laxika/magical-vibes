package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "696")
public class LukeCageHeroForHire extends Card {

    public LukeCageHeroForHire() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), CreateTokenEffect.ofTreasureToken(1));
    }
}
