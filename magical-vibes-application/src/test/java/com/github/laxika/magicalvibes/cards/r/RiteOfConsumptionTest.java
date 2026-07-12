package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiteOfConsumptionTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Deals damage equal to sacrificed creature's power and controller gains that much life")
    void dealsDamageAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sacrifice = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18); // 2 damage
        harness.assertLife(player1, 22); // gained 2 life
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage and life gain scale with the sacrificed creature's power including counters")
    void scalesWithCounters() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sacrifice = new Permanent(new GrizzlyBears()); // 2/2
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // becomes 5/5
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15); // 5 damage
        harness.assertLife(player1, 25); // gained 5 life
    }

    @Test
    @DisplayName("Can target yourself (target player)")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        Permanent sacrifice = new Permanent(new RagingGoblin()); // 1/1
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();

        // 1 damage to self, then gain 1 life: net back to 20
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
