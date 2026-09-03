package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "WOE", collectorNumber = "76")
public class VirtueOfKnowledge extends Card {

    public VirtueOfKnowledge() {
        setBackFaceCard(new VantressVisions());
        addCastingOption(new AdventureCast("{1}{U}"));
        addEffect(EffectSlot.STATIC, new ETBDoubleTriggerEffect(new CardTruePredicate(), false));
    }

    @Override
    public String getBackFaceClassName() {
        return "VantressVisions";
    }
}
