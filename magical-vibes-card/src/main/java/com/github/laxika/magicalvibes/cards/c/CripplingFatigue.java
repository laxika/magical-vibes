package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "58")
public class CripplingFatigue extends Card {

    public CripplingFatigue() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-2, -2));

        addCastingOption(new FlashbackCast(List.of(
                new ManaCastingCost("{1}{B}"),
                new LifeCastingCost(3)
        )));
    }
}
