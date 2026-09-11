package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ATQ", collectorNumber = "65")
public class StaffOfZegon extends Card {

    public StaffOfZegon() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new BoostTargetCreatureEffect(-2, 0)),
                "{3}, {T}: Target creature gets -2/-0 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
