package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaronSengirTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +2/+2 counter when a creature it damaged in combat dies")
    void getsCounterWhenDamagedCreatureDies() {
        harness.addToBattlefield(player1, new BaronSengir());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent baron = gd.playerBattlefields.get(player1.getId()).getFirst();
        baron.setSummoningSick(false);
        baron.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(baron.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(7);
    }

    @Test
    @DisplayName("No counter when the blocking creature survives")
    void noCounterWhenDamagedCreatureSurvives() {
        harness.addToBattlefield(player1, new BaronSengir());

        GrizzlyBears toughBlocker = new GrizzlyBears();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(8);
        harness.addToBattlefield(player2, toughBlocker);

        Permanent baron = gd.playerBattlefields.get(player1.getId()).getFirst();
        baron.setSummoningSick(false);
        baron.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(baron.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
    }

    @Test
    @DisplayName("Tap ability grants a regeneration shield to another Vampire")
    void regeneratesAnotherVampire() {
        addCreatureReady(player1, new BaronSengir());
        Permanent vampire = addCreatureReady(player1, new SengirVampire());

        harness.activateAbility(player1, 0, null, vampire.getId());
        harness.passBothPriorities();

        assertThat(vampire.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability can target an opponent's Vampire")
    void regeneratesOpponentVampire() {
        addCreatureReady(player1, new BaronSengir());
        Permanent opponentVampire = addCreatureReady(player2, new SengirVampire());

        harness.activateAbility(player1, 0, null, opponentVampire.getId());
        harness.passBothPriorities();

        assertThat(opponentVampire.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability cannot target Baron Sengir itself")
    void cannotRegenerateItself() {
        Permanent baron = addCreatureReady(player1, new BaronSengir());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, baron.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability cannot target a non-Vampire creature")
    void cannotRegenerateNonVampire() {
        addCreatureReady(player1, new BaronSengir());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
