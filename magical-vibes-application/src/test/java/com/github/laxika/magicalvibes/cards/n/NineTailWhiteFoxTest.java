package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NineTailWhiteFox.class, Island.class})
class NineTailWhiteFoxTest extends BaseCardTest {

    @Test
    void drawsCardWhenItDealsCombatDamageToAPlayer() {
        Island drawnCard = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        Permanent fox = addCreatureReady(player1, new NineTailWhiteFox());
        fox.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void combatDamageToACreatureDoesNotDrawACard() {
        Island libraryCard = new Island();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLibrary(player2, List.of(new Island()));
        Permanent fox = addCreatureReady(player1, new NineTailWhiteFox());
        fox.setAttacking(true);
        addCreatureReady(player2, new NineTailWhiteFox());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fox.getCard());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void drawsForItsControllerWhenPlayerTwoAttacks() {
        Island drawnCard = new Island();
        Island otherLibraryCard = new Island();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(otherLibraryCard));
        harness.setLibrary(player2, List.of(drawnCard));
        Permanent fox = addCreatureReady(player2, new NineTailWhiteFox());
        fox.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLibraryCard);
        harness.assertLife(player1, 18);
    }
}
