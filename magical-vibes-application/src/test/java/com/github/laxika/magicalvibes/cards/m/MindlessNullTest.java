package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindlessNullTest extends BaseCardTest {

    @Test
    @DisplayName("Can block when its controller controls a Vampire")
    void canBlockWithVampire() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new MindlessNull());
        addCreatureReady(player1, new BaronyVampire());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block when its controller controls no Vampire")
    void cannotBlockWithoutVampire() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new MindlessNull());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
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
}
