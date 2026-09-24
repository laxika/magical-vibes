package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwoHeadedGiantOfForiys.class, GrizzlyBears.class})
class TwoHeadedGiantOfForiysTest extends BaseCardTest {

    @Test
    @DisplayName("Two-Headed Giant of Foriys can block two attackers")
    void canBlockTwoAttackers() {
        Permanent giant = addCreatureReady(player2, new TwoHeadedGiantOfForiys());
        addAttacker();
        addAttacker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        ));

        assertThat(giant.isBlocking()).isTrue();
        assertThat(giant.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Two-Headed Giant of Foriys cannot block three attackers")
    void cannotBlockThreeAttackers() {
        addCreatureReady(player2, new TwoHeadedGiantOfForiys());
        addAttacker();
        addAttacker();
        addAttacker();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    private void addAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
    }
}
