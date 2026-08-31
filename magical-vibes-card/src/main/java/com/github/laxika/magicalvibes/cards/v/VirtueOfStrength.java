package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GarenbrigGrowth;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManaReflectionEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "197")
public class VirtueOfStrength extends Card {

    public VirtueOfStrength() {
        setBackFaceCard(new GarenbrigGrowth());
        addCastingOption(new AdventureCast("{G}"));
        addEffect(EffectSlot.STATIC, new ManaReflectionEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.BASIC)
        )), 3));
    }

    @Override
    public String getBackFaceClassName() {
        return "GarenbrigGrowth";
    }
}
