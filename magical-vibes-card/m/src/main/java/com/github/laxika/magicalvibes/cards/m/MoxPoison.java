package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "369")
@CardRegistration(set = "MB2", collectorNumber = "608")
public class MoxPoison extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("MoxPoison", new OracleData(
                "Mox Poison",
                CardType.ARTIFACT,
                Set.of(),
                "{0}",
                null,
                List.of(),
                List.of(),
                Set.of(),
                List.of(),
                "{T}: Add one mana of any color. You get two poison counters.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public MoxPoison() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardAnyColorManaEffect(),
                        new GivePoisonCountersEffect(2, PoisonRecipient.CONTROLLER)),
                "{T}: Add one mana of any color. You get two poison counters."));
    }
}
