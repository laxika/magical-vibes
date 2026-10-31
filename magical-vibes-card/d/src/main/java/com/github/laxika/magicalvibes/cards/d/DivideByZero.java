package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrPermanentToHandEffect;

@CardRegistration(set = "STX", collectorNumber = "41")
public class DivideByZero extends Card {

    public DivideByZero() {
        target(1, 1)
                .addSpellEffect(new ReturnTargetSpellOrPermanentToHandEffect(1));

        addSpellEffect(new LearnEffect());
    }
}
