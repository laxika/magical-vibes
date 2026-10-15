package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedHerringExchangeEffect;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "62")
public class RedHerringPlaytest extends Card {

    public RedHerringPlaytest() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new RedHerringExchangeEffect()),
                "{1}{U}: Exchange Red Herring from your hand with a permanent you control on the battlefield or a spell you control on the stack."
        ).withSourceStaysInHand().withRevealsSourceFromHand());
    }
}
