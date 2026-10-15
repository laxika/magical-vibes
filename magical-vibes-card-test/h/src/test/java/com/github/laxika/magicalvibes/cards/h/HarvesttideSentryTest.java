package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarvesttideSentry.class, GrizzlyBears.class, CrawWurm.class, HillGiant.class})
class HarvesttideSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Coven prevents power 2 or less creatures from blocking")
    void covenPreventsLowPowerBlockers() {
        addReadySentry();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(bears);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Coven allows creatures with power 3 or greater to block")
    void covenAllowsHighPowerBlockers() {
        addReadySentry();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        advanceToCombat(player1);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(giant);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Without Coven, power 2 creatures can block")
    void doesNotRestrictBlockersWithoutCoven() {
        addReadySentry();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Coven is checked again when the combat trigger resolves")
    void losingCovenBeforeResolutionAllowsBlocking() {
        addReadySentry();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        bears.setPowerModifier(1);
        resolveAllTriggers();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gaining coven after combat begins does not create a trigger")
    void gainingCovenTooLateAllowsBlocking() {
        addReadySentry();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new CrawWurm());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Losing coven after resolution does not remove the blocking restriction")
    void losingCovenAfterResolutionKeepsRestriction() {
        addReadySentry();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        resolveAllTriggers();
        bears.setPowerModifier(1);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blocker power is checked at declaration, including power modifiers")
    void pumpedPowerTwoCreatureCanBlock() {
        addReadySentry();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        resolveAllTriggers();
        blocker.setPowerModifier(1);
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not contribute to coven")
    void opponentPowersDoNotEnableCoven() {
        addReadySentry();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new CrawWurm());

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at end of turn")
    void restrictionExpiresAtEndOfTurn() {
        addReadySentry();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        resolveAllTriggers();
        bears.setPersistentPowerModifier(1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Coven does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        addReadySentry();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A power 3 creature reduced to power 2 cannot block")
    void reducedPowerThreeCreatureCannotBlock() {
        addReadySentry();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        advanceToCombat(player1);
        resolveAllTriggers();
        blocker.setPowerModifier(-1);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadySentry() {
        addCreatureReady(player1, new HarvesttideSentry());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
