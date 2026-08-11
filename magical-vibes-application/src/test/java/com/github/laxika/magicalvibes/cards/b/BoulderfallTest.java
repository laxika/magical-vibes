package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoulderfallTest extends BaseCardTest {

    @Test
    void dividesDamageAmongCreatureAndPlayer() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, Map.of(bears.getId(), 3, player2.getId(), 2));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsAllDamageToOnePlayer() {
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 5));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void assignmentsMustSumToFive() {
        harness.setHand(player1, List.of(new Boulderfall()));
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }
}
