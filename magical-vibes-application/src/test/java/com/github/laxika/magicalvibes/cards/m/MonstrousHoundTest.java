package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonstrousHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrous Hound can attack when its controller has more lands")
    void canAttackWithMoreLands() {
        addCreatureReady(player1, new MonstrousHound());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("Monstrous Hound cannot attack when its controller does not have more lands")
    void cannotAttackWithoutMoreLands() {
        addCreatureReady(player1, new MonstrousHound());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Monstrous Hound can block when its controller has more lands")
    void canBlockWithMoreLands() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player2, new MonstrousHound());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Monstrous Hound cannot block when its controller does not have more lands")
    void cannotBlockWithoutMoreLands() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player2, new MonstrousHound());
        harness.addToBattlefield(player2, new Forest());

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
