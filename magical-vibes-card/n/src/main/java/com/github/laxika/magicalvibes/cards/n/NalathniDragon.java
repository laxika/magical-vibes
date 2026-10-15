package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ActivationCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "197")
public class NalathniDragon extends Card {

    public NalathniDragon() {
        // {R}: This creature gets +1/+0 until end of turn. If this ability has been activated four or more times this turn,
        // sacrifice this creature at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(
                new BoostSelfEffect(1, 0),
                new ConditionalEffect(new ActivationCount(4, 0), new SacrificeSelfAtEndStepEffect())),
                "{R}: This creature gets +1/+0 until end of turn. If this ability has been activated four or more times this turn, sacrifice this creature at the beginning of the next end step."));
    }
}
