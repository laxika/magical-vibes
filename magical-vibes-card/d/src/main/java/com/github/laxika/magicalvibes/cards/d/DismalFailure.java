package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDiscardsEffect;

@CardRegistration(set = "PLC", collectorNumber = "39")
public class DismalFailure extends Card {

    public DismalFailure() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new TargetSpellControllerDiscardsEffect(1));
    }
}
