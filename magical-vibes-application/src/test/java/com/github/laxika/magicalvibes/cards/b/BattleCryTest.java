package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.cards.p.PaleBears;
import com.github.laxika.magicalvibes.cards.p.PalaceGuard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleCry.class, CircleOfProtectionRed.class, KjeldoranWarrior.class, PaleBears.class,
        PalaceGuard.class})
class BattleCryTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps controlled white creatures, not nonwhite, noncreature, or opponent's")
    void untapsOnlyControlledWhiteCreatures() {
        Permanent white = addCreatureReady(player1, new KjeldoranWarrior());
        white.tap();

        Permanent green = addCreatureReady(player1, new PaleBears());
        green.tap();

        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionRed());
        noncreature.tap();

        Permanent oppWhite = addCreatureReady(player2, new KjeldoranWarrior());
        oppWhite.tap();

        harness.castFromHand(player1, new BattleCry(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(white.isTapped()).isFalse();
        assertThat(green.isTapped()).isTrue();
        assertThat(noncreature.isTapped()).isTrue();
        assertThat(oppWhite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Whenever a creature blocks this turn, it gets +0/+1 until end of turn")
    void blockerGetsPlusZeroPlusOne() {
        Permanent attacker = addCreatureReady(player1, new PaleBears());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new KjeldoranWarrior());

        harness.castFromHand(player2, new BattleCry(), "{2}{W}");
        harness.passBothPriorities();

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        // Delayed trigger on the stack — resolve it.
        resolveAllTriggers();

        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blocker gets only one boost even when blocking multiple attackers")
    void blockerBoostTriggersOncePerBlockingCreature() {
        Permanent attackerOne = addCreatureReady(player1, new PaleBears());
        attackerOne.setAttacking(true);

        Permanent attackerTwo = addCreatureReady(player1, new PaleBears());
        attackerTwo.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new PalaceGuard());

        harness.castFromHand(player1, new BattleCry(), "{2}{W}");
        harness.passBothPriorities();

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerOneIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerOne);
        int attackerTwoIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerTwo);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, attackerOneIdx),
                new BlockerAssignment(blockerIdx, attackerTwoIdx)));

        resolveAllTriggers();

        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
        assertThat(blocker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Two Battle Cry spells give a blocker +0/+2")
    void multipleBattleCrySourcesStack() {
        Permanent attacker = addCreatureReady(player1, new PaleBears());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new KjeldoranWarrior());

        harness.castFromHand(player1, new BattleCry(), "{2}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new BattleCry(), "{2}{W}");
        harness.passBothPriorities();

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        resolveAllTriggers();

        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
        assertThat(blocker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Blocker boost wears off at end of turn")
    void blockerBoostExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new PaleBears());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new KjeldoranWarrior());

        harness.castFromHand(player2, new BattleCry(), "{2}{W}");
        harness.passBothPriorities();

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        resolveAllTriggers();

        assertThat(blocker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getToughnessModifier()).isZero();
    }
}
