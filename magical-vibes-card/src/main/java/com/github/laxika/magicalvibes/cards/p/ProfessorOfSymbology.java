package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;

@CardRegistration(set = "STX", collectorNumber = "24")
public class ProfessorOfSymbology extends Card {

    public ProfessorOfSymbology() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LearnEffect());
    }
}
