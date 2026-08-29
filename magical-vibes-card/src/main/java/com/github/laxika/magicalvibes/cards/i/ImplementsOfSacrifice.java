package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "88")
public class ImplementsOfSacrifice extends Card {

    public ImplementsOfSacrifice() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect(2)),
                "{1}, {T}, Sacrifice Implements of Sacrifice: Add two mana of any one color."
        ));
    }
}
