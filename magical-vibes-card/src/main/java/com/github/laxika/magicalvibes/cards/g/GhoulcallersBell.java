package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "224")
public class GhoulcallersBell extends Card {

    public GhoulcallersBell() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new MillEffect(1, MillRecipient.CONTROLLER), new MillEffect(1, MillRecipient.EACH_OPPONENT)),
                "{T}: Each player mills a card."
        ));
    }
}
