package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "GRN", collectorNumber = "220")
public class WhisperAgent extends Card {

    public WhisperAgent() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));
    }
}
