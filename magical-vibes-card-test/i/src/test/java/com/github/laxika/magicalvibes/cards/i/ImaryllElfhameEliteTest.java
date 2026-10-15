package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Overrun;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImaryllElfhameElite.class, LlanowarElves.class, GrizzlyBears.class, Overrun.class})
class ImaryllElfhameEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Imaryll does not count itself for its attack boost")
    void noBoostWithoutOtherElves() {
        Permanent imaryll = addCreatureReady(player1, new ImaryllElfhameElite());
        addCreatureReady(player2, new LlanowarElves());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(imaryll.getPowerModifier()).isZero();
        assertThat(imaryll.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts other Elves when the attack trigger resolves and fixes the boost then")
    void countsElvesAtResolution() {
        Permanent imaryll = addCreatureReady(player1, new ImaryllElfhameElite());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(elf);
        gd.playerGraveyards.get(player1.getId()).add(elf.getCard());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new LlanowarElves());
        resolveAllTriggers();

        assertThat(imaryll.getPowerModifier()).isEqualTo(2);
        assertThat(imaryll.getToughnessModifier()).isEqualTo(2);
        addCreatureReady(player1, new LlanowarElves());
        assertThat(imaryll.getPowerModifier()).isEqualTo(2);
        assertThat(imaryll.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Trample does not prevent Imaryll assigning all damage as though unblocked")
    void canAssignAllDamageAsThoughUnblockedWithTrample() {
        harness.setLife(player2, 20);
        Permanent imaryll = addCreatureReady(player1, new ImaryllElfhameElite());
        Permanent blocker = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Overrun()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castAndResolveSorcery(player1, 0, 0);

        imaryll.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 6));

        harness.assertLife(player2, 14);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(imaryll.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Gets +1/+1 for each other Elf you control when it attacks")
    void boostsForOtherElvesYouControl() {
        Permanent imaryll = addCreatureReady(player1, new ImaryllElfhameElite());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player2, new LlanowarElves());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(imaryll.getPowerModifier()).isEqualTo(2);
        assertThat(imaryll.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent imaryll = addCreatureReady(player1, new ImaryllElfhameElite());
        addCreatureReady(player1, new LlanowarElves());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(imaryll.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(imaryll.getPowerModifier()).isZero();
        assertThat(imaryll.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blocked Imaryll can assign combat damage to the defending player")
    void blockedImaryllCanAssignDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent imaryll = addCreatureReady(player1, new ImaryllElfhameElite());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        imaryll.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 3));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A blocked Imaryll can assign combat damage to its blocker")
    void blockedImaryllCanAssignDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent imaryll = addCreatureReady(player1, new ImaryllElfhameElite());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        imaryll.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
