package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "346")
public class SpuzzemStrategist extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("SpuzzemStrategist", new OracleData(
                "Spuzzem Strategist",
                CardType.CREATURE,
                Set.of(),
                "{3}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.SPUZZEM, CardSubtype.ADVISOR),
                "You make all choices for Spuzzems you control.",
                4,
                4,
                Set.of(),
                null,
                null,
                null));
    }

    public SpuzzemStrategist() {
        // Spuzzem choices are already made by their controller in the engine's default choice flow.
    }
}
