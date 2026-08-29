package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "34a")
@CardRegistration(set = "FEM", collectorNumber = "34b")
@CardRegistration(set = "FEM", collectorNumber = "34c")
@CardRegistration(set = "FEM", collectorNumber = "34d")
public class BasalThrull extends Card {

    public BasalThrull() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.BLACK, 2)),
                "{T}, Sacrifice this creature: Add {B}{B}."
        ));
    }
}
