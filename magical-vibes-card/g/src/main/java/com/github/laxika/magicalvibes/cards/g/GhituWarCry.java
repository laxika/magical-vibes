package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "78")
public class GhituWarCry extends Card {

    public GhituWarCry() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostTargetCreatureEffect(1, 0)),
                "{R}: Target creature gets +1/+0 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
