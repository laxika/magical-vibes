package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.PokeyTheScallywaggEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "361")
@CardRegistration(set = "MB2", collectorNumber = "600")
public class PokeyTheScallywagg extends Card {

    static {
        Card.registerOracle("PokeyTheScallywagg", new OracleData(
                "Pokey, the Scallywagg",
                CardType.CREATURE,
                Set.of(),
                "{U}{R}",
                CardColor.BLUE,
                List.of(CardColor.BLUE, CardColor.RED),
                List.of(CardColor.BLUE, CardColor.RED),
                Set.of(CardSupertype.LEGENDARY),
                List.of(CardSubtype.BRUSHWAGG, CardSubtype.PIRATE),
                "Menace\n"
                        + "If you would flip a coin, you may instead roll a d20. 1–10 is tails and 11–20 is heads. "
                        + "(It counts as rolling a die, not flipping a coin.)\n"
                        + "If you would roll a d20, you may instead flip a coin. Tails is 1 and heads is 20. "
                        + "(It counts as flipping a coin, not rolling a d20.)",
                2,
                2,
                Set.of(Keyword.MENACE),
                null,
                null,
                null));
    }

    public PokeyTheScallywagg() {
        addEffect(EffectSlot.STATIC, new PokeyTheScallywaggEffect());
    }
}
