package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "139")
public class OvergrownArch extends Card {

    public OvergrownArch() {
        // "{T}: You gain 1 life."
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new GainLifeEffect(1)),
                "{T}: You gain 1 life."));

        // "{2}, Sacrifice this creature: Learn."
        addActivatedAbility(new ActivatedAbility(
                false, "{2}", List.of(new SacrificeSelfCost(), new LearnEffect()),
                "{2}, Sacrifice this creature: Learn."
        ));
    }
}
