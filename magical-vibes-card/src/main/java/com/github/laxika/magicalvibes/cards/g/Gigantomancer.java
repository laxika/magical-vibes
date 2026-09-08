package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "184")
public class Gigantomancer extends Card {

    public Gigantomancer() {
        // {1}: Target creature you control has base power and toughness 7/7 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SetBasePowerToughnessEffect(7, 7)),
                "{1}: Target creature you control has base power and toughness 7/7 until end of turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
