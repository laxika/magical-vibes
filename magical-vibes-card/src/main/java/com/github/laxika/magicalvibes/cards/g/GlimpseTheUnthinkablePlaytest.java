package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "355")
@CardRegistration(set = "MB2", collectorNumber = "594")
public class GlimpseTheUnthinkablePlaytest extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds under #355. */
    static {
        Card.registerOracle("GlimpseTheUnthinkablePlaytest", new OracleData(
                "Glimpse, the Unthinkable",
                CardType.CREATURE,
                Set.of(),
                "{2}{U}{B}",
                CardColor.BLACK,
                List.of(CardColor.BLACK, CardColor.BLUE),
                List.of(CardColor.BLACK, CardColor.BLUE),
                Set.of(CardSupertype.LEGENDARY),
                List.of(CardSubtype.ILLUSION, CardSubtype.ROGUE),
                "Shroud (This creature can't be the target of spells or abilities.)\n"
                        + "Glimpse, the Unthinkable can't be chosen.\n"
                        + "The name Glimpse, the Unthinkable can't be chosen.",
                4,
                5,
                Set.of(Keyword.SHROUD),
                null,
                null,
                null));
    }

    public GlimpseTheUnthinkablePlaytest() {
    }
}
