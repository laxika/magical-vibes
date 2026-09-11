package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThanosTheMadTitan.class, LlanowarElves.class, GrizzlyBears.class})
class ThanosTheMadTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Power-up adds counters and destroys other creatures with odd mana values")
    void powerUpDestroysOddCreatures() {
        Permanent thanos = harness.enterBattlefieldAndReturn(player1, new ThanosTheMadTitan());
        harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentOdd = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent opponentEven = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ODD");

        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(thanos);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentEven);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentOdd);
    }

    @Test
    @DisplayName("Power-up can destroy other creatures with even mana values")
    void powerUpDestroysEvenCreatures() {
        Permanent thanos = harness.enterBattlefieldAndReturn(player1, new ThanosTheMadTitan());
        Permanent opponentOdd = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent opponentEven = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "EVEN");

        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(thanos);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentOdd);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentEven);
    }
}
