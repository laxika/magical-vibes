package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DefiantKhenra;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaraudingBoneslasher.class, DefiantKhenra.class})
class MaraudingBoneslasherTest extends BaseCardTest {

    @Test
    @DisplayName("Can block when controlling another Zombie")
    void canBlockWithAnotherZombie() {
        addCreatureReady(player2, new DefiantKhenra());
        addCreatureReady(player1, new MaraudingBoneslasher());
        addCreatureReady(player1, new MaraudingBoneslasher());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block when it is the only Zombie controlled")
    void cannotBlockAsOnlyZombie() {
        addCreatureReady(player2, new DefiantKhenra());
        addCreatureReady(player1, new MaraudingBoneslasher());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack freely even as the only Zombie (restriction is block-only)")
    void canAttackAsOnlyZombie() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MaraudingBoneslasher());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("An opponent's Zombie does not allow blocking")
    void cannotBlockWithOnlyOpponentsZombie() {
        addCreatureReady(player2, new MaraudingBoneslasher());
        addCreatureReady(player1, new MaraudingBoneslasher());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another friendly creature must be a Zombie to allow blocking")
    void cannotBlockWithNonZombieCompanion() {
        addCreatureReady(player2, new DefiantKhenra());
        addCreatureReady(player1, new MaraudingBoneslasher());
        addCreatureReady(player1, new DefiantKhenra());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped friendly Zombie still allows blocking")
    void canBlockWithTappedZombie() {
        addCreatureReady(player2, new DefiantKhenra());
        var blocker = addCreatureReady(player1, new MaraudingBoneslasher());
        addCreatureReady(player1, new MaraudingBoneslasher()).tap();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
