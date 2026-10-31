package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "70")
public class StepBetweenWorlds extends Card {

    public StepBetweenWorlds() {
        addSpellEffect(new EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect(7));
        addSpellEffect(new ExileSpellEffect());

        // Plot {4}{U}{U}
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{U}{U}"))));
    }
}
