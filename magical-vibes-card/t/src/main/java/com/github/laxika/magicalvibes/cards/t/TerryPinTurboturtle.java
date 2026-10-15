package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.AllActivatedAbilitiesCanBeActivatedAtInstantSpeedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "364")
@CardRegistration(set = "MB2", collectorNumber = "603")
public class TerryPinTurboturtle extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("TerryPinTurboturtle", new OracleData(
                "Terry Pin, Turboturtle",
                CardType.CREATURE,
                Set.of(),
                "{2}{U}{R}",
                CardColor.BLUE,
                List.of(CardColor.BLUE, CardColor.RED),
                List.of(CardColor.BLUE, CardColor.RED),
                Set.of(CardSupertype.LEGENDARY),
                List.of(CardSubtype.TURTLE, CardSubtype.ATHLETE),
                "Flash\n"
                        + "Haste\n"
                        + "You may ignore the words \"Activate only as a sorcery\" while activating abilities.",
                4,
                3,
                Set.of(Keyword.FLASH, Keyword.HASTE),
                null,
                null,
                null));
    }

    public TerryPinTurboturtle() {
        addEffect(EffectSlot.STATIC, new AllActivatedAbilitiesCanBeActivatedAtInstantSpeedEffect());
    }
}
