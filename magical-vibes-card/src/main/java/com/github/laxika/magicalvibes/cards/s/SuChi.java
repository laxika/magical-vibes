package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "ATQ", collectorNumber = "66")
@CardRegistration(set = "ME1", collectorNumber = "168")
public class SuChi extends Card {

    public SuChi() {
        addEffect(EffectSlot.ON_DEATH, new AwardManaEffect(ManaColor.COLORLESS, 4));
    }
}
