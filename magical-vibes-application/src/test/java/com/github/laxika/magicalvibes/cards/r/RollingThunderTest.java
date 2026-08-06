package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RollingThunderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage divided between a creature and a player")
    void dividesDamageAmongCreatureAndPlayer() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 10);

        // X = 3 -> 2 to the 2/2 (lethal), 1 to the opponent.
        harness.castSorceryForX(player1, 0, 3, Map.of(bears.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("All X damage may go to a single target")
    void dealsAllDamageToOnePlayer() {
        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 10);

        harness.castSorceryForX(player1, 0, 4, Map.of(player2.getId(), 4));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Assignments must sum to X")
    void assignmentsMustSumToX() {
        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 10);

        assertThatThrownBy(() ->
                harness.castSorceryForX(player1, 0, 3, Map.of(player2.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }
}
