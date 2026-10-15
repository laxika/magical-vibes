package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BrambleWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SporebackWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WelcomingVampire.class, BrambleWurm.class, Forest.class, SporebackWolf.class,
        WeddingFestivity.class})
class WelcomingVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when a creature with power 2 or less enters under your control")
    void drawsWhenSmallCreatureEnters() {
        addVampire();
        seedLibrary(1);

        castSporebackWolf(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Does not trigger for larger creatures or creatures entering under an opponent's control")
    void ignoresLargerAndOpposingCreatures() {
        addVampire();
        seedLibrary(1);

        harness.setHand(player1, List.of(new BrambleWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SporebackWolf()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        addVampire();
        seedLibrary(2);

        castSporebackWolf(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
        castSporebackWolf(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers again on a later turn")
    void triggersAgainOnLaterTurn() {
        addVampire();
        seedLibrary(3);

        castSporebackWolf(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);

        advanceTurn();
        advanceTurn();

        harness.forceActivePlayer(player1);
        castSporebackWolf(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotDrawForItsOwnEntry() {
        seedLibrary(1);
        harness.setHand(player1, List.of(new WelcomingVampire()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Vampire has its own once-per-turn limit")
    void eachVampireDrawsIndependently() {
        addVampire();
        addVampire();
        seedLibrary(2);

        castSporebackWolf(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A larger creature does not consume the once-per-turn trigger")
    void largerEntryDoesNotPreventLaterDraw() {
        addVampire();
        seedLibrary(1);
        harness.setHand(player1, List.of(new BrambleWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        castSporebackWolf(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Checks power including continuous boosts when a creature enters")
    void boostedCreatureAboveTwoPowerDoesNotTrigger() {
        addVampire();
        harness.addToBattlefield(player1, new WeddingFestivity());
        seedLibrary(1);

        castSporebackWolf(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void addVampire() {
        harness.addToBattlefield(player1, new WelcomingVampire());
    }

    private void seedLibrary(int count) {
        harness.setLibrary(player1, IntStream.range(0, count)
                .mapToObj(i -> new Forest()).toList());
    }

    private void castSporebackWolf(Player player) {
        harness.setHand(player, List.of(new SporebackWolf()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
