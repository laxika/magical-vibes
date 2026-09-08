package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AwakenTheAges;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/** Strife Scholar // Awaken the Ages (SOS 131). */
@CardRegistration(set = "SOS", collectorNumber = "131")
public class StrifeScholarAwakenTheAges extends Card {

    public StrifeScholarAwakenTheAges() {
        setBackFaceCard(new AwakenTheAges());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AwakenTheAges";
    }
}
