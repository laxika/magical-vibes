package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViashinoRunner.class, GorillaWarrior.class})
class ViashinoRunnerTest extends BaseCardTest {
    @Test
    @DisplayName("Viashino Runner cannot be blocked by only one creature")
    void cannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new ViashinoRunner());
        addCreatureReady(player2, new GorillaWarrior());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Viashino Runner can be blocked by two creatures")
    void canBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new ViashinoRunner());
        Permanent blockerOne = addCreatureReady(player2, new GorillaWarrior());
        Permanent blockerTwo = addCreatureReady(player2, new GorillaWarrior());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Viashino Runner can be blocked by more than two creatures")
    void canBeBlockedByThreeCreatures() {
        addCreatureReady(player1, new ViashinoRunner());
        Permanent blockerOne = addCreatureReady(player2, new GorillaWarrior());
        Permanent blockerTwo = addCreatureReady(player2, new GorillaWarrior());
        Permanent blockerThree = addCreatureReady(player2, new GorillaWarrior());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
        assertThat(blockerThree.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Each attacking Viashino Runner requires its own two blockers")
    void twoBlockersCannotBeSplitAcrossTwoRunners() {
        addCreatureReady(player1, new ViashinoRunner());
        addCreatureReady(player1, new ViashinoRunner());
        addCreatureReady(player2, new GorillaWarrior());
        addCreatureReady(player2, new GorillaWarrior());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Unblocked Viashino Runner deals 3 damage to defending player")
    void unblockedDealsThreeDamage() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new ViashinoRunner());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 17);
    }
}
