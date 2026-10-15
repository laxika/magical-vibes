package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;

@CardRegistration(set = "INV", collectorNumber = "145")
public class GoblinSpy extends Card {

    public GoblinSpy() {
        addEffect(EffectSlot.STATIC, new PlayWithTopCardRevealedEffect());
    }
}
