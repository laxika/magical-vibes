package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CliffThreader;
import com.github.laxika.magicalvibes.cards.v.VampireLacerator;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindlessNull.class, CliffThreader.class, VampireLacerator.class})
class MindlessNullTest extends BaseCardTest {

    @Test
    @DisplayName("Can block when its controller controls a Vampire")
    void canBlockWithVampire() {
        addCreatureReady(player2, new CliffThreader());
        addCreatureReady(player1, new MindlessNull());
        addCreatureReady(player1, new VampireLacerator());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block when its controller controls no Vampire")
    void cannotBlockWithoutVampire() {
        addCreatureReady(player2, new CliffThreader());
        addCreatureReady(player1, new MindlessNull());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack without controlling a Vampire")
    void canAttackWithoutVampire() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MindlessNull());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("An opponent's Vampire does not enable blocking")
    void cannotBlockWithOpponentsVampire() {
        addCreatureReady(player2, new CliffThreader());
        addCreatureReady(player2, new VampireLacerator());
        addCreatureReady(player1, new MindlessNull());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Vampire still enables blocking")
    void canBlockWithTappedVampire() {
        addCreatureReady(player2, new CliffThreader());
        var blocker = addCreatureReady(player1, new MindlessNull());
        addCreatureReady(player1, new VampireLacerator()).setTapped(true);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
