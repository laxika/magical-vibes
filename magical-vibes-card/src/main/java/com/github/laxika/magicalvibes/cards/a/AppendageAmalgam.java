package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "DSK", collectorNumber = "83")
public class AppendageAmalgam extends Card {

    public AppendageAmalgam() {
        addEffect(EffectSlot.ON_ATTACK, new SurveilEffect(1));
    }
}
