package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MoonrageBrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "7")
@CardRegistration(set = "DBL", collectorNumber = "7")
public class BrutalCathar extends Card {

    public BrutalCathar() {
        setBackFaceCard(new MoonrageBrute());

        target(TargetFilters.creatureAnOpponentControls());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetPermanentUntilSourceLeavesEffect());
        addEffect(EffectSlot.ON_TRANSFORM_TO_FRONT_FACE,
                new ExileTargetPermanentUntilSourceLeavesEffect());

    }

    @Override
    public String getBackFaceClassName() {
        return "MoonrageBrute";
    }
}
