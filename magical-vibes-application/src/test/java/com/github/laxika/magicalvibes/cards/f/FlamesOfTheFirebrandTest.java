package com.github.laxika.magicalvibes.cards.f;

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

class FlamesOfTheFirebrandTest extends BaseCardTest {

    @Test
    @DisplayName("Deals all 3 damage to a single target")
    void dealsAllDamageToOneTarget() {
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 3));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Divides damage between a creature and a player")
    void dividesDamageBetweenCreatureAndPlayer() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Divides 1 damage each among three targets")
    void dividesDamageAmongThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0,
                Map.of(first.getId(), 1, second.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(p -> p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Assignments must sum to exactly 3")
    void assignmentsMustSumToThree() {
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }
}
