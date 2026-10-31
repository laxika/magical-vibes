package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdventureToCreatureCardsInHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "347")
public class StartingTownNpc extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("StartingTownNpc", new OracleData(
                "Starting Town NPC",
                CardType.CREATURE,
                Set.of(),
                "{2}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.HUMAN, CardSubtype.PEASANT),
                "Each creature card in your hand has a {1}{G} Adventure sorcery named Fetch Herbs with "
                        + "\"You gain 2 life.\" Each creature card you cast from exile enters the battlefield "
                        + "with an additional +1/+1 counter on it.",
                3,
                3,
                Set.of(),
                null,
                null,
                null));
    }

    public StartingTownNpc() {
        addStaticEffect(new GrantAdventureToCreatureCardsInHandEffect());
        addStaticEffect(new ControlledPermanentsEnterWithAdditionalCountersEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentCastFromZonePredicate(Zone.EXILE))),
                1));
    }
}
