package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "119")
public class RuthlessKnave extends Card {

    public RuthlessKnave() {
        // {2}{B}, Sacrifice a creature: Create two Treasure tokens.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsCreaturePredicate(),
                                "Sacrifice a creature"
                        ),
                        CreateTokenEffect.ofTreasureToken(2)
                ),
                "{2}{B}, Sacrifice a creature: Create two Treasure tokens."
        ));

        // Sacrifice three Treasures: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(3,
                                new PermanentHasSubtypePredicate(CardSubtype.TREASURE)
                        ),
                        new DrawCardEffect(1)
                ),
                "Sacrifice three Treasures: Draw a card."
        ));
    }
}
