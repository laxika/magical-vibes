package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DrownInShapelessness;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PurpleCrystalCrab.class, Island.class, Shock.class, DrownInShapelessness.class})
class PurpleCrystalCrabTest extends BaseCardTest {

    @Test
    void drawsCardWhenItDies() {
        Island drawnCard = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new PurpleCrystalCrab());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Purple-Crystal Crab"));

        harness.assertInGraveyard(player1, "Purple-Crystal Crab");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void drawsForOpponentWhenOpponentsCrabDies() {
        Island drawnCard = new Island();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addToBattlefield(player2, new PurpleCrystalCrab());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Purple-Crystal Crab"));

        harness.assertInGraveyard(player2, "Purple-Crystal Crab");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void returningToHandDoesNotDrawCard() {
        PurpleCrystalCrab crab = new PurpleCrystalCrab();
        Island libraryCard = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addToBattlefield(player1, crab);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DrownInShapelessness()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Purple-Crystal Crab"));

        harness.assertNotOnBattlefield(player1, "Purple-Crystal Crab");
        harness.assertNotInGraveyard(player1, "Purple-Crystal Crab");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(crab);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
    }
}
