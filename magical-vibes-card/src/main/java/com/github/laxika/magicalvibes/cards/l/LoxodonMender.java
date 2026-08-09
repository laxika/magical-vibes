package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "12")
public class LoxodonMender extends Card {

    public LoxodonMender() {
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
