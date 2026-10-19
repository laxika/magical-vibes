package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "70")
public class StepBetweenWorlds extends Card {

    public StepBetweenWorlds() {
        addEffect(EffectSlot.SPELL, new EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect(7));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());

        // Plot {4}{U}{U}
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{U}{U}"))));
    }
}
