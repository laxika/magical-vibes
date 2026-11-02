package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEmbalmEqualToManaCostToCreatureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "58")
public class Naktamun extends Card {

    public Naktamun() {
        addStaticEffect(new GrantEmbalmEqualToManaCostToCreatureCardsEffect());
        addTrigger(Triggers.chaosEnsues(),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
