package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "174")
public class SatyrHedonist extends Card {

    public SatyrHedonist() {
        // {R}, Sacrifice this creature: Add {R}{R}{R}.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.RED, 3)),
                "{R}, Sacrifice this creature: Add {R}{R}{R}."
        ));
    }
}
