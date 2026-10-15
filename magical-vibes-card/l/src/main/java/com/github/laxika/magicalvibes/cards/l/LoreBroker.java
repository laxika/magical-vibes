package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "57")
public class LoreBroker extends Card {

    public LoreBroker() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new EachPlayerDrawsCardEffect(1), new DiscardEffect(1, DiscardRecipient.EACH_PLAYER)),
                "{T}: Each player draws a card, then discards a card."));
    }
}
