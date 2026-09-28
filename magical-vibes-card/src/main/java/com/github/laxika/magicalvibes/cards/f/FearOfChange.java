package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAnotherCreatureAndConjureRandomCreatureEffect;

@CardRegistration(set = "YDSK", collectorNumber = "21")
public class FearOfChange extends Card {

    public FearOfChange() {
        ExileAnotherCreatureAndConjureRandomCreatureEffect effect =
                new ExileAnotherCreatureAndConjureRandomCreatureEffect();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, effect);
        addEffect(EffectSlot.ON_DEATH, effect);
    }
}
