package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YellowScarvesTroops.class, ForestBear.class})
class YellowScarvesTroopsTest extends BaseCardTest {

    @Test
    @DisplayName("Yellow Scarves Troops cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent troops = addCreatureReady(player2, new YellowScarvesTroops());

        Permanent attacker = addCreatureReady(player1, new ForestBear());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
