package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesSycoraxCommanderVillainousChoiceEffect;

@CardRegistration(set = "WHO", collectorNumber = "161")
public class SycoraxCommander extends Card {

    public SycoraxCommander() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOpponentFacesSycoraxCommanderVillainousChoiceEffect());
    }
}
