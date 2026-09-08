package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "89")
public class BloodlineCulling extends Card {

    public BloodlineCulling() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -5/-5 until end of turn",
                        new BoostTargetCreatureEffect(-5, -5),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Creature tokens get -2/-2 until end of turn",
                        new BoostAllCreaturesEffect(-2, -2, new PermanentIsTokenPredicate()))
        )));
    }
}
