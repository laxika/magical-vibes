package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "145")
public class BaneAlleyBroker extends Card {

    public BaneAlleyBroker() {
        // {T}: Draw a card, then exile a card from your hand face down.
        // The face-down exile is only visible to this creature's controller, which also covers the
        // "You may look at cards exiled with this creature" clause. No control-loss graveyard
        // clause here, hence the false flag.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DrawCardEffect(1), new ExileCardFromHandFaceDownWithSourceEffect(false)),
                "{T}: Draw a card, then exile a card from your hand face down."));

        // {U}{B}, {T}: Return a card exiled with this creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(true, "{U}{B}",
                List.of(new PutCardExiledWithSourceIntoHandEffect()),
                "{U}{B}, {T}: Return a card exiled with this creature to its owner's hand."));
    }
}
