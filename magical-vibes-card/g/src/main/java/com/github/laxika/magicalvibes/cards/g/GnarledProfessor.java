package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;

@CardRegistration(set = "STX", collectorNumber = "133")
public class GnarledProfessor extends Card {

    public GnarledProfessor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LearnEffect());
    }
}
