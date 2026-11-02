package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ParleyEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "40")
@CardRegistration(set = "MOC", collectorNumber = "127")
public class CutthroatNegotiator extends Card {

    public CutthroatNegotiator() {
        addTrigger(Triggers.attacks(),
                new ParleyEffect(CreateTokenEffect.ofTappedTreasureToken(1)));
    }
}
