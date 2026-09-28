package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SneakyHomunculus.class, FugitiveWizard.class, GrizzlyBears.class})
class SneakyHomunculusTest extends BaseCardTest {

    @Test
    @DisplayName("Sneaky Homunculus can block a creature with power 1")
    void canBlockLowPowerCreature() {
        Permanent homunculus = addCreatureReady(player2, new SneakyHomunculus());

        addCreatureReady(player1, new FugitiveWizard()); // 1/1
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(homunculus.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sneaky Homunculus cannot block a creature with power 2 or greater")
    void cannotBlockHighPowerCreature() {
        addCreatureReady(player2, new SneakyHomunculus());

        addCreatureReady(player1, new GrizzlyBears()); // 2/2
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with power 1 or less");
    }

    @Test
    @DisplayName("Sneaky Homunculus can be blocked by a creature with power 1")
    void canBeBlockedByLowPowerCreature() {
        addCreatureReady(player1, new SneakyHomunculus());
        Permanent blockerPerm = addCreatureReady(player2, new FugitiveWizard()); // 1/1

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sneaky Homunculus cannot be blocked by a creature with power 2 or greater")
    void cannotBeBlockedByHighPowerCreature() {
        addCreatureReady(player1, new SneakyHomunculus());
        addCreatureReady(player2, new GrizzlyBears()); // 2/2

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Sneaky Homunculus cannot block after a power 1 attacker reaches power 2")
    void cannotBlockAttackerAfterItsPowerIncreases() {
        addCreatureReady(player2, new SneakyHomunculus());

        Permanent attacker = addCreatureReady(player1, new FugitiveWizard());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with power 1 or less");
    }

    @Test
    @DisplayName("Sneaky Homunculus cannot be blocked after a power 1 blocker reaches power 2")
    void cannotBeBlockedByBlockerAfterItsPowerIncreases() {
        addCreatureReady(player1, new SneakyHomunculus());
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }
}
