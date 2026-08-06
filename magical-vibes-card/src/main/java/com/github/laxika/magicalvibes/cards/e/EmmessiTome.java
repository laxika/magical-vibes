package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "284")
public class EmmessiTome extends Card {

    public EmmessiTome() {
        addActivatedAbility(new ActivatedAbility(true, "{5}", List.of(new DrawCardEffect(2), new DiscardEffect(1, DiscardRecipient.CONTROLLER)), "{5}, {T}: Draw two cards, then discard a card."));
    }
}
