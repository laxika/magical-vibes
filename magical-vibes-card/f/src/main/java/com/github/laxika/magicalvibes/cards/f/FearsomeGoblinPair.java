package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;

@CardRegistration(set = "HOB", collectorNumber = "156")
public class FearsomeGoblinPair extends Card {

    public FearsomeGoblinPair() {
        addEffect(EffectSlot.ON_DEATH, new AmassGoblinsEffect(4));
    }
}
