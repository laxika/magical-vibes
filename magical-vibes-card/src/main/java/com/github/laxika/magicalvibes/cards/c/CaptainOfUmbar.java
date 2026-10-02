package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "173")
public class CaptainOfUmbar extends Card {

    public CaptainOfUmbar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{1}, {T}: Draw a card, then discard a card."
        ));
    }
}
