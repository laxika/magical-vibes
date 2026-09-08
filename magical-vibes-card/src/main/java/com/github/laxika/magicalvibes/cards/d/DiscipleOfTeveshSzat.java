package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "55")
public class DiscipleOfTeveshSzat extends Card {

    public DiscipleOfTeveshSzat() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(-1, -1)),
                "{T}: Target creature gets -1/-1 until end of turn.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{B}{B}",
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(-6, -6)),
                "{4}{B}{B}, {T}, Sacrifice this creature: Target creature gets -6/-6 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
