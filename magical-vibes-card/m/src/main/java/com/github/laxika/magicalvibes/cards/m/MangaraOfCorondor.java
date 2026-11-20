package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "28")
@CardRegistration(set = "TSR", collectorNumber = "27")
public class MangaraOfCorondor extends Card {

    public MangaraOfCorondor() {
        // {T}: Exile Mangara and target permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileTargetPermanentEffect(Subjects.allOf(Subjects.source(), Subjects.target()))),
                "{T}: Exile Mangara and target permanent.",
                TargetFilters.permanent()
        ));
    }
}
