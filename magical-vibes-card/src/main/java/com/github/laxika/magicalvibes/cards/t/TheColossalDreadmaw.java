package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.CastCreatureCardsAsNamedCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "336")
@CardRegistration(set = "MB2", collectorNumber = "573")
public class TheColossalDreadmaw extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("TheColossalDreadmaw", new OracleData(
                "The Colossal Dreadmaw",
                CardType.CREATURE,
                Set.of(),
                "{4}{G}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(CardSupertype.LEGENDARY),
                List.of(CardSubtype.DINOSAUR),
                "Trample\nYou may cast creature cards from your hand as though they were the card "
                        + "Colossal Dreadmaw. (They become Colossal Dreadmaws.)",
                6,
                6,
                Set.of(Keyword.TRAMPLE),
                null,
                null,
                null));
    }

    public TheColossalDreadmaw() {
        addEffect(EffectSlot.STATIC,
                new CastCreatureCardsAsNamedCardEffect(new ColossalDreadmaw()));
    }
}
