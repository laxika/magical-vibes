package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AetherChaser;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HinterlandDrake.class, Ornithopter.class, AetherChaser.class})
class HinterlandDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Hinterland Drake cannot block an artifact creature")
    void cannotBlockArtifactCreature() {
        addCreatureReady(player2, new HinterlandDrake());
        Permanent attacker = addCreatureReady(player1, new Ornithopter());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures that aren't artifacts");
    }

    @Test
    @DisplayName("Hinterland Drake can block a nonartifact creature")
    void canBlockNonartifactCreature() {
        Permanent drake = addCreatureReady(player2, new HinterlandDrake());
        Permanent attacker = addCreatureReady(player1, new AetherChaser());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(drake.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Hinterland Drake can block a nonartifact flying creature")
    void canBlockNonartifactFlyingCreature() {
        Permanent drake = addCreatureReady(player2, new HinterlandDrake());
        Permanent attacker = addCreatureReady(player1, new HinterlandDrake());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(drake.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An artifact creature can block an attacking Hinterland Drake")
    void canBeBlockedByArtifactCreature() {
        Permanent blocker = addCreatureReady(player2, new Ornithopter());
        Permanent attacker = addCreatureReady(player1, new HinterlandDrake());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
