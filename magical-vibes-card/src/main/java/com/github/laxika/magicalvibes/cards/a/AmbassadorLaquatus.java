package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "65")
public class AmbassadorLaquatus extends Card {

    public AmbassadorLaquatus() {
        addActivatedAbility(new ActivatedAbility(false, "{3}", List.of(new MillEffect(3, MillRecipient.TARGET_PLAYER)), "{3}: Target player mills three cards."));
    }
}
