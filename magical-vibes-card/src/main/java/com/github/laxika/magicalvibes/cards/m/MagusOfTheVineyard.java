package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;

@CardRegistration(set = "FUT", collectorNumber = "132")
public class MagusOfTheVineyard extends Card {

    public MagusOfTheVineyard() {
        addEffect(EffectSlot.EACH_PRECOMBAT_MAIN_TRIGGERED,
                new AwardManaToActivePlayerEffect(ManaColor.GREEN, 2));
    }
}
