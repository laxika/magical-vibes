package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "43")
public class MagusOfTheBazaar extends Card {

    public MagusOfTheBazaar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{0}",
                List.of(new DrawCardEffect(2), new DiscardEffect(3, DiscardRecipient.CONTROLLER)),
                "{T}: Draw two cards, then discard three cards."
        ));
    }
}
