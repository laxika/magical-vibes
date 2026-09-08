package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerFromTargetPowerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "51")
public class RiptideMangler extends Card {

    public RiptideMangler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new SetSelfBasePowerFromTargetPowerEffect()),
                "{1}{U}: Change this creature's base power to target creature's power.",
                TargetFilters.creature()
        ));
    }
}
