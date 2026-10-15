package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NestRobber;
import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeadstrongBrute.class, NestRobber.class, SailorOfMeans.class})
class HeadstrongBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Headstrong Brute cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent brute = harness.addToBattlefieldAndReturn(player2, new HeadstrongBrute());
        brute.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NestRobber());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Has menace when controller controls another Pirate")
    void hasMenaceWithAnotherPirate() {
        harness.addToBattlefield(player1, new HeadstrongBrute());
        harness.addToBattlefield(player1, new SailorOfMeans());

        Permanent brute = findPermanent(player1, "Headstrong Brute");
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Does not have menace when alone (no other Pirate)")
    void noMenaceWithoutAnotherPirate() {
        harness.addToBattlefield(player1, new HeadstrongBrute());

        Permanent brute = findPermanent(player1, "Headstrong Brute");
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Does not have menace with a non-Pirate creature")
    void noMenaceWithNonPirate() {
        harness.addToBattlefield(player1, new HeadstrongBrute());
        harness.addToBattlefield(player1, new NestRobber());

        Permanent brute = findPermanent(player1, "Headstrong Brute");
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Loses menace when the other Pirate leaves the battlefield")
    void losesMenaceWhenPirateLeaves() {
        harness.addToBattlefield(player1, new HeadstrongBrute());
        harness.addToBattlefield(player1, new SailorOfMeans());

        Permanent brute = findPermanent(player1, "Headstrong Brute");
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isTrue();

        // Remove the other Pirate
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Sailor of Means"));

        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Pirate does not grant menace")
    void opponentPirateDoesNotGrantMenace() {
        harness.addToBattlefield(player1, new HeadstrongBrute());
        harness.addToBattlefield(player2, new SailorOfMeans());

        Permanent brute = findPermanent(player1, "Headstrong Brute");
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Two Headstrong Brutes grant each other menace")
    void twoHeadstrongBrutesGrantEachOtherMenace() {
        harness.addToBattlefield(player1, new HeadstrongBrute());
        harness.addToBattlefield(player1, new HeadstrongBrute());

        List<Permanent> brutes = findPermanents(player1, "Headstrong Brute");

        assertThat(brutes).hasSize(2);
        assertThat(gqs.hasKeyword(gd, brutes.get(0), Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, brutes.get(1), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Another Pirate prevents a single creature from blocking Headstrong Brute")
    void singleBlockerIsIllegalWithAnotherPirate() {
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new HeadstrongBrute());
        brute.setAttacking(true);
        harness.addToBattlefield(player1, new SailorOfMeans());
        harness.addToBattlefield(player2, new NestRobber());
        harness.addToBattlefield(player2, new NestRobber());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Two creatures can block Headstrong Brute with another Pirate")
    void twoBlockersAreLegalWithAnotherPirate() {
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new HeadstrongBrute());
        brute.setAttacking(true);
        harness.addToBattlefield(player1, new SailorOfMeans());
        harness.addToBattlefield(player2, new NestRobber());
        harness.addToBattlefield(player2, new NestRobber());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A single creature can block after the other Pirate leaves during combat")
    void singleBlockerIsLegalAfterPirateLeavesDuringCombat() {
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new HeadstrongBrute());
        brute.setAttacking(true);
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new SailorOfMeans());
        harness.addToBattlefield(player2, new NestRobber());
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(pirate);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

}
