package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RampagingCeratops.class})
class RampagingCeratopsTest extends BaseCardTest {

    @Test
    @DisplayName("Rampaging Ceratops cannot be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThreeCreatures() {
        Permanent ceratops = addAttackingCeratops();
        addBlockers(3);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");

        assertThat(ceratops.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Rampaging Ceratops can be blocked by three creatures")
    void canBeBlockedByThreeCreatures() {
        addAttackingCeratops();
        addBlockers(3);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    private Permanent addAttackingCeratops() {
        Permanent ceratops = addCreatureReady(player1, new RampagingCeratops());
        ceratops.setAttacking(true);
        return ceratops;
    }

    private void addBlockers(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player2, new RampagingCeratops());
        }
    }

    @Test
    @DisplayName("Rampaging Ceratops cannot be blocked by one creature")
    void cannotBeBlockedByOneCreature() {
        addAttackingCeratops();
        addBlockers(3);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    @DisplayName("Rampaging Ceratops may be left unblocked even when three blockers are available")
    void mayBeLeftUnblocked() {
        Permanent ceratops = addAttackingCeratops();
        addBlockers(3);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(gqs.isBlockedByAnyCreature(gd, ceratops)).isFalse();
    }

    @Test
    @DisplayName("Rampaging Ceratops can be blocked by more than three creatures")
    void canBeBlockedByFourCreatures() {
        addAttackingCeratops();
        addBlockers(4);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0),
                new BlockerAssignment(3, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allSatisfy(blocker -> assertThat(blocker.isBlocking()).isTrue());
    }
}
