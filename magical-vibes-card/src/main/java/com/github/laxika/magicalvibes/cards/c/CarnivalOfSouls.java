package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "UDS", collectorNumber = "55")
public class CarnivalOfSouls extends Card {

    public CarnivalOfSouls() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, new LoseLifeEffect(1));
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new AwardManaEffect(ManaColor.BLACK));
    }
}
