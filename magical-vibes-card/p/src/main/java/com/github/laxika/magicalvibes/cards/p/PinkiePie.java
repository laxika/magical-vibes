package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PartyAlwaysFullEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1539")
public class PinkiePie extends Card {

    public PinkiePie() {
        // Card art is not represented by the engine, so every controller spell is treated as
        // matching the "smile in its art" condition.
        addTrigger(Triggers.youCastSpell().anySpell(), CreateTokenEffect.ofTreasureToken(1, true));
        addStaticEffect(new PartyAlwaysFullEffect());
    }
}
