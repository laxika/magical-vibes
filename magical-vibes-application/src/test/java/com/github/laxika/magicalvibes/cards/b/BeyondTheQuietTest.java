package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({BeyondTheQuiet.class, GrizzlyBears.class, MindStone.class})
class BeyondTheQuietTest extends BaseCardTest {

    @Test
    void exilesAllCreaturesAndSpacecraftButLeavesOtherPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, spacecraft());
        harness.addToBattlefield(player2, new MindStone());

        harness.setHand(player1, List.of(new BeyondTheQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Test Spacecraft");
        harness.assertOnBattlefield(player2, "Mind Stone");
    }

    private Card spacecraft() {
        Card card = new Card();
        card.setName("Test Spacecraft");
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.SPACECRAFT));
        return card;
    }
}
