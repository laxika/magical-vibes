package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "19")
public class Metallurgeon extends Card {

    public Metallurgeon() {
        // {W}, {T}: Regenerate target artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new RegenerateEffect(true)),
                "{W}, {T}: Regenerate target artifact.",
                TargetFilters.artifact()
        ));
    }
}
