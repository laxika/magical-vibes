package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "31")
public class TimeSieve extends Card {

    public TimeSieve() {
        // {T}, Sacrifice five artifacts: Take an extra turn after this one.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(5, new PermanentIsArtifactPredicate()),
                        new ControllerExtraTurnEffect(1)
                ),
                "{T}, Sacrifice five artifacts: Take an extra turn after this one."
        ));
    }
}
