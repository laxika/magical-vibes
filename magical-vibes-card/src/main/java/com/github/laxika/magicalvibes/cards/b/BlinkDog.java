package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "3")
public class BlinkDog extends Card {

    public BlinkDog() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{W}",
                List.of(new PhaseOutEffect(PhaseOutSubject.SOURCE)),
                "{3}{W}: This creature phases out."));
    }
}
