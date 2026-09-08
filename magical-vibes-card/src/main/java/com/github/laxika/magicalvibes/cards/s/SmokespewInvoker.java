package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "81")
public class SmokespewInvoker extends Card {

    public SmokespewInvoker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}{B}",
                List.of(new BoostTargetCreatureEffect(-3, -3)),
                "{7}{B}: Target creature gets -3/-3 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
