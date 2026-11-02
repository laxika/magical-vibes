package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLS", collectorNumber = "98")
public class CloudCover extends Card {

    public CloudCover() {
        addTrigger(Triggers.anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new MayEffect(ReturnToHandEffect.triggering(),
                        "Return that permanent to its owner's hand?"));
    }
}
