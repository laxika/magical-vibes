package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "374")
@CardRegistration(set = "MB2", collectorNumber = "563")
public class Gobland extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("Gobland", new OracleData(
                "Gobland",
                CardType.LAND,
                Set.of(CardType.CREATURE),
                null,
                CardColor.RED,
                List.of(CardColor.RED),
                List.of(CardColor.RED),
                Set.of(),
                List.of(CardSubtype.MOUNTAIN, CardSubtype.GOBLIN),
                "(Gobland isn't a spell, it's affected by summoning sickness, and it has \"{T}: Add {R}.\")\n"
                        + "Gobland can't block.",
                2,
                1,
                Set.of(),
                null,
                null,
                null));
    }

    public Gobland() {
        // {T}: Add {R} comes from its basic land type (CR 305.6).
        addStaticEffect(new CantBlockEffect());
    }
}
