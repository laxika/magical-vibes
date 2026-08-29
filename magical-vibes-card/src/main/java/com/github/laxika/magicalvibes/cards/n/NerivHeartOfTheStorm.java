package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromCreaturesEnteredThisTurnEffect;

@CardRegistration(set = "TDM", collectorNumber = "210")
public class NerivHeartOfTheStorm extends Card {

    public NerivHeartOfTheStorm() {
        addEffect(EffectSlot.STATIC, new DoubleDamageFromCreaturesEnteredThisTurnEffect());
    }
}
