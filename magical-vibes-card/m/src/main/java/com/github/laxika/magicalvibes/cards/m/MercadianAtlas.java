package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DidntPlayLandThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "305")
public class MercadianAtlas extends Card {

    public MercadianAtlas() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new DidntPlayLandThisTurn(),
                new MayEffect(new DrawCardEffect(), "Draw a card?")));
    }
}
