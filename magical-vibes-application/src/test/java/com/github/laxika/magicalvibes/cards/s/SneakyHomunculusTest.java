package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.Lightbringer;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SneakyHomunculus.class, Mossdog.class, Lightbringer.class})
class SneakyHomunculusTest extends BaseCardTest {

    @Test
    @DisplayName("Sneaky Homunculus can block a creature with power 1")
    void canBlockLowPowerCreature() {
        Permanent homunculus = addCreatureReady(player2, new SneakyHomunculus());

        Permanent atkPerm = addCreatureReady(player1, new Mossdog()); // 1/1
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(homunculus.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sneaky Homunculus cannot block a creature with power 2 or greater")
    void cannotBlockHighPowerCreature() {
        addCreatureReady(player2, new SneakyHomunculus());

        Permanent atkPerm = addCreatureReady(player1, new Lightbringer()); // 2/2
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with power 1 or less");
    }

    @Test
    @DisplayName("Sneaky Homunculus can be blocked by a creature with power 1")
    void canBeBlockedByLowPowerCreature() {
        Permanent homunculus = addCreatureReady(player1, new SneakyHomunculus());
        homunculus.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new Mossdog()); // 1/1

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sneaky Homunculus cannot be blocked by a creature with power 2 or greater")
    void cannotBeBlockedByHighPowerCreature() {
        Permanent homunculus = addCreatureReady(player1, new SneakyHomunculus());
        homunculus.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new Lightbringer()); // 2/2

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }
}
