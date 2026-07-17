package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "334")
@CardRegistration(set = "6ED", collectorNumber = "300")
@CardRegistration(set = "9ED", collectorNumber = "304")
@CardRegistration(set = "8ED", collectorNumber = "307")
@CardRegistration(set = "7ED", collectorNumber = "308")
@CardRegistration(set = "5ED", collectorNumber = "390")
public class Millstone extends Card {

    public Millstone() {
        addActivatedAbility(new ActivatedAbility(true, "{2}", List.of(new MillEffect(2, MillRecipient.TARGET_PLAYER)), "{2}, {T}: Target player mills two cards."));
    }
}
