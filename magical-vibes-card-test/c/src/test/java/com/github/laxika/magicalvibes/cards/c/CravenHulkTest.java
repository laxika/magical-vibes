package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CravenHulk.class, GrizzlyBears.class, TurnToFrog.class})
class CravenHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Craven Hulk can attack alone")
    void canAttackAlone() {
        harness.setLife(player2, 20);

        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new CravenHulk());
        hulk.setSummoningSick(false);

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Craven Hulk can't block alone")
    void cantBlockAlone() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent hulk = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        hulk.setSummoningSick(false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Craven Hulk can block with another creature")
    void canBlockWithAnotherCreature() {
        Permanent attacker1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker1.setSummoningSick(false);
        attacker1.setAttacking(true);

        Permanent attacker2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker2.setSummoningSick(false);
        attacker2.setAttacking(true);

        Permanent hulk = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        hulk.setSummoningSick(false);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        assertThat(hulk.isBlocking()).isTrue();
        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An idle companion does not let Craven Hulk block alone")
    void cannotBlockWithUndeclaredCompanion() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new CravenHulk());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block alone");
    }

    @Test
    @DisplayName("Two Craven Hulks can block the same attacker together")
    void canBlockSameAttackerTogether() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Craven Hulk can block alone after Turn to Frog removes its abilities")
    void canBlockAloneAfterLosingAbilities() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hulk = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, hulk.getId());
        assertThat(gqs.hasLostAllAbilities(gd, hulk)).isTrue();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(hulk.isBlocking()).isTrue();
    }
}
