package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManticoreOfTheGauntletTest extends BaseCardTest {

    // ===== ETB: -1/-1 counter on your creature + 3 damage to opponent =====

    @Test
    @DisplayName("ETB puts a -1/-1 counter on a creature you control and deals 3 damage to an opponent")
    void etbCountersOwnCreatureAndDamagesOpponent() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new ManticoreOfTheGauntlet()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        // Target order matches declaration: counter-on-your-creature first, damage second.
        gs.playCard(gd, player1, 0, 0, null, null, List.of(elemental.getId(), player2.getId()), List.of());
        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        // Air Elemental (4/4) with one -1/-1 counter → 3/3.
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(elemental.getEffectivePower()).isEqualTo(3);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(3);
        // Opponent takes 3 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== The counter target must be a creature you control =====

    @Test
    @DisplayName("ETB cannot put the counter on a creature you don't control")
    void etbCannotCounterOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentCreature = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ManticoreOfTheGauntlet()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(opponentCreature, player2.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    // ===== Damage can hit a planeswalker instead of an opponent =====

    @Test
    @DisplayName("ETB can deal 3 damage to a planeswalker")
    void etbDamagesPlaneswalker() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent elspeth = new Permanent(new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(elspeth);

        harness.setHand(player1, List.of(new ManticoreOfTheGauntlet()));
        harness.addMana(player1, ManaColor.RED, 5);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(elemental.getId(), elspeth.getId()), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 4 - 3
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}
