package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2007")
@CardRegistration(set = "SLD", collectorNumber = "2263")
public class TreasonousOgre extends Card {

    public TreasonousOgre() {
        // Pay 3 life: Add {R}.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(3), new AwardManaEffect(ManaColor.RED)),
                "Pay 3 life: Add {R}."
        ));
    }
}
