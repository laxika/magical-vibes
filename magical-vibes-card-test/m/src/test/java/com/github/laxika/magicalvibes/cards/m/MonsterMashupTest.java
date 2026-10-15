package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonsterMashup.class, AirElemental.class, GrizzlyBears.class})
class MonsterMashupTest extends BaseCardTest {

    @Test
    @DisplayName("Reach allows Monster Mashup to block a creature with flying")
    void reachAllowsBlockingFlyingCreature() {
        Permanent flyer = addCreatureReady(player1, new AirElemental());
        flyer.setAttacking(true);
        addCreatureReady(player2, new MonsterMashup());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Menace requires at least two blockers")
    void menaceRequiresTwoBlockers() {
        Permanent attacker = addCreatureReady(player1, new MonsterMashup());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace permits two blockers")
    void menacePermitsTwoBlockers() {
        Permanent attacker = addCreatureReady(player1, new MonsterMashup());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Menace permits the defender to leave Monster Mashup unblocked")
    void menacePermitsNoBlockers() {
        Permanent attacker = addCreatureReady(player1, new MonsterMashup());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Menace permits more than two blockers")
    void menacePermitsThreeBlockers() {
        Permanent attacker = addCreatureReady(player1, new MonsterMashup());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Reach does not prevent creatures without flying from blocking Monster Mashup")
    void reachDoesNotGrantFlying() {
        Permanent attacker = addCreatureReady(player1, new MonsterMashup());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MonsterMashup());
        addCreatureReady(player2, new MonsterMashup());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }
}
