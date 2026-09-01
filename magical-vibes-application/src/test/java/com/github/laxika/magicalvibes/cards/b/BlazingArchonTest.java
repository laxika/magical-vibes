package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlazingArchon.class, GrizzlyBears.class})
class BlazingArchonTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures cannot attack Blazing Archon's controller")
    void creaturesCannotAttackController() {
        harness.addToBattlefield(player2, new BlazingArchon());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The restriction does not stop the controller's creatures from attacking an opponent")
    void creaturesCanAttackOpponent() {
        harness.addToBattlefield(player1, new BlazingArchon());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
    }
}
