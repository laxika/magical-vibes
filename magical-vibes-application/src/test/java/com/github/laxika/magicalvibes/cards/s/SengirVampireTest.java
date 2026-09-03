package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SengirVampire.class, GrizzlyBears.class, Terror.class})
class SengirVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when a creature it damaged in combat dies")
    void getsCounterWhenDamagedCreatureDiesInCombat() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        sengir.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, sengir)).isEqualTo(5);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, sengir)).isEqualTo(5);
    }

    @Test
    @DisplayName("Triggers when a creature damaged by Sengir Vampire dies later the same turn")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());

        GrizzlyBears toughBlocker = new GrizzlyBears();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(5);
        Permanent blocker = addCreatureReady(player2, toughBlocker);
        sengir.setAttacking(true);

        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, sengir)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger when an undamaged creature dies")
    void doesNotTriggerWhenUndamagedCreatureDies() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when a damaged creature dies on a later turn")
    void doesNotTriggerWhenDamagedCreatureDiesOnLaterTurn() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        GrizzlyBears toughBlocker = new GrizzlyBears();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(5);
        Permanent blocker = addCreatureReady(player2, toughBlocker);

        sengir.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
