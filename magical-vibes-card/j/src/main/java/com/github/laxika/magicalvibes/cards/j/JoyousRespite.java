package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "CHK", collectorNumber = "216")
public class JoyousRespite extends Card {

    public JoyousRespite() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }
}
