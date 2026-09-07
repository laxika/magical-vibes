package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AlterFate;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "ELD", collectorNumber = "99")
public class OrderOfMidnight extends Card {

    public OrderOfMidnight() {
        setBackFaceCard(new AlterFate());
        addCastingOption(new AdventureCast("{1}{B}"));
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "AlterFate";
    }
}
