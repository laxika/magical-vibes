package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "358")
@CardRegistration(set = "MB2", collectorNumber = "597")
public class LutriPauperOtter extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds under #358. */
    static {
        Card.registerOracle("LutriPauperOtter", new OracleData(
                "Lutri, Pauper Otter",
                CardType.CREATURE,
                Set.of(),
                "{3}{U/R}{U/R}",
                CardColor.BLUE,
                List.of(CardColor.BLUE, CardColor.RED),
                List.of(CardColor.BLUE, CardColor.RED),
                Set.of(CardSupertype.LEGENDARY),
                List.of(CardSubtype.ELEMENTAL, CardSubtype.OTTER),
                "Companion \u2014 Your starting deck contains no cards with a silver, gold, orange, or purple "
                        + "expansion symbol. (If this card is your chosen companion, you may put it into your hand "
                        + "from outside the game for {3} as a sorcery.)\n"
                        + "When Lutri, Pauper Otter enters the battlefield, discard your hand, then draw three cards.",
                3,
                4,
                Set.of(),
                null,
                null,
                null));
    }

    public LutriPauperOtter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DiscardHandEffect(),
                new DrawCardEffect(3)));
    }
}
