package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "294")
@CardRegistration(set = "TPR", collectorNumber = "225")
public class LotusPetal extends Card {

    public LotusPetal() {
        // "{T}, Sacrifice this artifact: Add one mana of any color."
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new AwardAnyColorManaEffect()
                ),
                "{T}, Sacrifice Lotus Petal: Add one mana of any color."
        ));
    }
}
