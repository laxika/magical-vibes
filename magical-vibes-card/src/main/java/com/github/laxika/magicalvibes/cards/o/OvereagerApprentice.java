package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "154")
public class OvereagerApprentice extends Card {

    public OvereagerApprentice() {
        // Discard a card, Sacrifice this creature: Add {B}{B}{B}.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new SacrificeSelfCost(),
                        new AwardManaEffect(ManaColor.BLACK, 3)
                ),
                "Discard a card, Sacrifice this creature: Add {B}{B}{B}."
        ));
    }
}
