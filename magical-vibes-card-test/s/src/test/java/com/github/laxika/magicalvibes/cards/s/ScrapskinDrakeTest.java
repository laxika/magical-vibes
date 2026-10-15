package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.Gloomwidow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrapskinDrake.class, AirElemental.class, GrizzlyBears.class, Gloomwidow.class})
class ScrapskinDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Scrapskin Drake can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent drake = addCreatureReady(player2, new ScrapskinDrake());

        Permanent attacker = addCreatureReady(player1, new AirElemental());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(drake.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Scrapskin Drake cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new ScrapskinDrake());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Scrapskin Drake with flying can only be blocked by flyers or reach")
    void flyingEvasion() {
        Permanent drake = addCreatureReady(player1, new ScrapskinDrake());
        drake.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reach alone does not let an attacker be blocked by Scrapskin Drake")
    void cannotBlockReachCreatureWithoutFlying() {
        Permanent drake = addCreatureReady(player2, new ScrapskinDrake());
        Permanent attacker = addCreatureReady(player1, new Gloomwidow());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
        assertThat(drake.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Scrapskin Drake can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        Permanent drake = addCreatureReady(player1, new ScrapskinDrake());
        drake.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Gloomwidow());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Scrapskin Drake can be blocked by another flying Scrapskin Drake")
    void canBeBlockedByFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new ScrapskinDrake());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ScrapskinDrake());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
