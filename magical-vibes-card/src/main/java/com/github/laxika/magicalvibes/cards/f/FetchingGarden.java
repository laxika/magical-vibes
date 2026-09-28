package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.EnteredFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "372")
@CardRegistration(set = "MB2", collectorNumber = "611")
public class FetchingGarden extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds under collector number 372. */
    static {
        Card.registerOracle("FetchingGarden", new OracleData(
                "Fetching Garden",
                CardType.LAND,
                Set.of(),
                null,
                null,
                List.of(),
                List.of(CardColor.GREEN, CardColor.WHITE),
                Set.of(),
                List.of(CardSubtype.FOREST, CardSubtype.PLAINS),
                "({T}: Add {G} or {W}.)\nFetching Garden enters the battlefield tapped if it was played from your hand.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public FetchingGarden() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new EnteredFromZone(Zone.HAND), new EntersTappedEffect()));
    }
}
