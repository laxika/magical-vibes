package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "GRN", collectorNumber = "33")
public class CitywatchSphinx extends Card {

    public CitywatchSphinx() {
        addEffect(EffectSlot.ON_DEATH, new SurveilEffect(2));
    }
}
