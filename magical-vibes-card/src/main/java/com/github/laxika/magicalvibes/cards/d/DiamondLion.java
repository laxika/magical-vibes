package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "225")
public class DiamondLion extends Card {

    public DiamondLion() {
        // {T}, Discard your hand, Sacrifice this creature: Add three mana of any one color.
        // Activate only as an instant — instant speed is the engine default, so no timing restriction.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DiscardHandCost(), new SacrificeSelfCost(), new AwardAnyColorManaEffect(3)),
                "{T}, Discard your hand, Sacrifice Diamond Lion: Add three mana of any one color. Activate only as an instant."
        ));
    }
}
