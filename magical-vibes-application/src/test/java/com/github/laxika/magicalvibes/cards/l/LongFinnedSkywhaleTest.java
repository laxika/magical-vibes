package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HighspireArtisan;
import com.github.laxika.magicalvibes.cards.t.ThrivingTurtle;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongFinnedSkywhale.class, ThrivingTurtle.class, WindDrake.class, HighspireArtisan.class})
class LongFinnedSkywhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Long-Finned Skywhale cannot be blocked by a nonflying creature")
    void cannotBeBlockedByNonflyingCreature() {
        attackingSkywhale();
        harness.addToBattlefield(player2, new ThrivingTurtle());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Long-Finned Skywhale can be blocked by a flying creature")
    void canBeBlockedByFlyingCreature() {
        attackingSkywhale();
        harness.addToBattlefield(player2, new WindDrake());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Long-Finned Skywhale cannot block a creature without flying")
    void cannotBlockNonflyingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ThrivingTurtle());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new LongFinnedSkywhale());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Long-Finned Skywhale can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent skywhale = harness.addToBattlefieldAndReturn(player2, new LongFinnedSkywhale());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(skywhale.getBlockingTargets()).containsExactly(0);
    }

    @Test
    @DisplayName("A creature with reach can block Long-Finned Skywhale")
    void canBeBlockedByCreatureWithReach() {
        attackingSkywhale();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HighspireArtisan());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getBlockingTargets()).containsExactly(0);
    }

    @Test
    @DisplayName("Reach does not let a nonflying attacker be blocked by Long-Finned Skywhale")
    void cannotBlockNonflyingCreatureWithReach() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HighspireArtisan());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new LongFinnedSkywhale());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void attackingSkywhale() {
        Permanent skywhale = harness.addToBattlefieldAndReturn(player1, new LongFinnedSkywhale());
        skywhale.setSummoningSick(false);
        skywhale.setAttacking(true);
    }
}
