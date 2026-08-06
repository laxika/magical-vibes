package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "211")
public class ThroneOfGeth extends Card {

    public ThroneOfGeth() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false), new ProliferateEffect()),
                "{T}, Sacrifice an artifact: Proliferate."
        ));
    }
}
