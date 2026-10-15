package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "14")
@CardRegistration(set = "LCC", collectorNumber = "33")
public class XavierSalInfestedCaptain extends Card {

    public XavierSalInfestedCaptain() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromControlledPermanentCost(1, null, true),
                        new PopulateEffect()
                ),
                "{T}, Remove a counter from another permanent you control: Populate. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new ProliferateEffect()
                ),
                "{T}, Sacrifice another creature: Proliferate. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
