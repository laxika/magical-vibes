package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "235")
public class GoblinFirebomb extends Card {

    public GoblinFirebomb() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{7}, {T}, Sacrifice this artifact: Destroy target permanent."
        ));
    }
}
