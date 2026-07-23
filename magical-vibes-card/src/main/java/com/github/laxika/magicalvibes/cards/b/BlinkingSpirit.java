package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "8")
@CardRegistration(set = "5ED", collectorNumber = "12")
@CardRegistration(set = "ICE", collectorNumber = "8")
public class BlinkingSpirit extends Card {

    public BlinkingSpirit() {
        addActivatedAbility(new ActivatedAbility(false, "{0}", List.of(ReturnToHandEffect.self()), "{0}: Return Blinking Spirit to its owner's hand."));
    }
}
