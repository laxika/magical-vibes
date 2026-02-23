package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnAtEndStepEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "28")
public class ArgentSphinx extends Card {

    public ArgentSphinx() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new ExileSelfAndReturnAtEndStepEffect()),
                "Metalcraft \u2014 {U}: Exile Argent Sphinx. Return it to the battlefield under your control at the beginning of the next end step. Activate only if you control three or more artifacts.",
                ActivationTimingRestriction.METALCRAFT
        ));
    }
}
