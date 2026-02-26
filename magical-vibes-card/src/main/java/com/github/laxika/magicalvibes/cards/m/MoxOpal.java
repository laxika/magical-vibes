package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "179")
public class MoxOpal extends Card {

    public MoxOpal() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "Metalcraft — {T}: Add one mana of any color. Activate only if you control three or more artifacts.",
                ActivationTimingRestriction.METALCRAFT
        ));
    }
}
