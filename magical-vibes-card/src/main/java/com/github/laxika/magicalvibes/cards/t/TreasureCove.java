package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

/**
 * Treasure Cove — back face of Treasure Map.
 * Land.
 * {T}: Add {C}.
 * {T}, Sacrifice a Treasure: Draw a card.
 */
public class TreasureCove extends Card {

    public TreasureCove() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {T}, Sacrifice a Treasure: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.TREASURE),
                                "Sacrifice a Treasure"
                        ),
                        new DrawCardEffect(1)
                ),
                "{T}, Sacrifice a Treasure: Draw a card."
        ));
    }
}
