package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "7")
public class DalkovanPackbeasts extends Card {

    public DalkovanPackbeasts() {
        addTrigger(Triggers.attacks(),
                new CreateTokenEffect(
                3, "Warrior", 1, 1, CardColor.RED, List.of(CardSubtype.WARRIOR), true
        ),
                new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
