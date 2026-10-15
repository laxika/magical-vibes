package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThanosTheMadTitan.class, LlanowarElves.class, GrizzlyBears.class, Ornithopter.class})
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
    @Test
    void evenChoiceDestroysZeroManaValueCreaturesOnBothBattlefields() {
        Permanent thanos = harness.enterBattlefieldAndReturn(player1, new ThanosTheMadTitan());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "EVEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(thanos);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void powerUpCannotBeActivatedAgainEvenBeforeFirstActivationResolves() {
        harness.enterBattlefieldAndReturn(player1, new ThanosTheMadTitan());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ODD");
    }

    @Test
    void powerUpRequiresFullCostWhenThanosDidNotEnterThisTurn() {
        Permanent thanos = harness.addToBattlefieldAndReturn(player1, new ThanosTheMadTitan());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ODD");

        assertThat(thanos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(thanos);
    }

    @Test
    void destructionStillResolvesWhenThanosLeavesBeforeResolution() {
        Permanent thanos = harness.enterBattlefieldAndReturn(player1, new ThanosTheMadTitan());
        harness.addToBattlefield(player2, new LlanowarElves());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, thanos));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ODD");

        harness.assertInGraveyard(player1, "Thanos, the Mad Titan");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
    }
}
