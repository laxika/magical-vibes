package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HammerheadSharkTest extends BaseCardTest {

    @Test
    @DisplayName("Hammerhead Shark can't attack unless the defending player controls an Island")
    void cannotAttackWithoutDefendingIsland() {
        addCreatureReady(player1, new HammerheadShark());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Hammerhead Shark can attack when the defending player controls an Island")
    void canAttackWithDefendingIsland() {
        addCreatureReady(player1, new HammerheadShark());
        harness.addToBattlefield(player2, new Island());

        declareAttackers(player1, List.of(0));
    }
}
