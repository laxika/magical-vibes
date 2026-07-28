package com.github.laxika.magicalvibes.cards.m;

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

class MeteorShowerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X plus 1 damage divided between a creature and a player")
    void dividesDamageAmongCreatureAndPlayer() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MeteorShower()));
        harness.addMana(player1, ManaColor.RED, 10);

        // X = 2 -> 3 damage total: 2 to the 2/2 (lethal), 1 to the opponent.
        harness.castSorceryForX(player1, 0, 2, Map.of(bears.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("With X = 0 it still deals 1 damage")
    void dealsOneDamageWithXZero() {
        harness.setHand(player1, List.of(new MeteorShower()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorceryForX(player1, 0, 0, Map.of(player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Assignments must sum to X plus 1")
    void assignmentsMustSumToXPlusOne() {
        harness.setHand(player1, List.of(new MeteorShower()));
        harness.addMana(player1, ManaColor.RED, 10);

        // X = 2 -> 3 damage total, but only 2 is assigned.
        assertThatThrownBy(() ->
                harness.castSorceryForX(player1, 0, 2, Map.of(player2.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each X symbol is paid separately")
    void requiresManaForBothXSymbols() {
        harness.setHand(player1, List.of(new MeteorShower()));
        // {X}{X}{R} with X = 3 costs 7 mana; only 6 is available.
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() ->
                harness.castSorceryForX(player1, 0, 3, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }
}
