package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.FortressCrab;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({AbattoirGhoul.class, CruelEdict.class, FortressCrab.class, GrizzlyBears.class})
class AbattoirGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to dying creature's toughness when it kills in combat")
    void gainsLifeWhenKillingInCombat() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new AbattoirGhoul());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ghoul.setSummoningSick(false);
        ghoul.setAttacking(true);

        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Pass through first strike damage, regular damage, and trigger resolution
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Grizzly Bears (toughness 2) should be dead
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Controller gains life equal to Grizzly Bears' toughness (2)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Gains life equal to toughness when damaged creature dies later in the turn")
    void gainsLifeWhenDamagedCreatureDiesLater() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new AbattoirGhoul());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FortressCrab());
        ghoul.setSummoningSick(false);
        ghoul.setAttacking(true);

        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve first strike damage — blocker survives (3 damage on 6 toughness)
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fortress Crab");

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Now kill the blocker with Cruel Edict later in the turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        // Pass 2: resolve triggered ability — gain life
        harness.passBothPriorities();

        // Blocker should be dead
        harness.assertInGraveyard(player2, "Fortress Crab");

        // Controller gains life equal to the dying creature's toughness (6)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    @Test
    @DisplayName("Does not gain life when a creature not damaged by it dies")
    void doesNotGainLifeForUndamagedCreature() {
        harness.addToBattlefield(player1, new AbattoirGhoul());
        harness.addToBattlefield(player2, new GrizzlyBears());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Kill the creature without Abattoir Ghoul dealing damage to it
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");

        // No life gained — Abattoir Ghoul didn't damage the creature
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Life gain uses the dying creature's toughness including counters")
    void gainsLifeIncludingToughnessFromCounters() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new AbattoirGhoul());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        ghoul.setSummoningSick(false);
        ghoul.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 3);
        harness.assertOnBattlefield(player1, "Abattoir Ghoul");
    }
}
