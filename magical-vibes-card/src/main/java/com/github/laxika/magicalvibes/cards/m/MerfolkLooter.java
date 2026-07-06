package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "92")
@CardRegistration(set = "M10", collectorNumber = "61")
public class MerfolkLooter extends Card {

    public MerfolkLooter() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER)), "{T}: Draw a card, then discard a card."));
    }
}
