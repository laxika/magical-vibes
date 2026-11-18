package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaraldUnitesTheElves.class, JasperaSentinel.class, AxgardCavalry.class,
        SnowCoveredIsland.class, TyvarKell.class})
class HaraldUnitesTheElvesTest extends BaseCardTest {

    @Test
    void chapterIMillsAndMayReturnAnElf() {
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new SnowCoveredIsland(),
                new SnowCoveredIsland(), new SnowCoveredIsland()));
        harness.setGraveyard(player1, List.of(new JasperaSentinel(), new AxgardCavalry()));
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Jaspera Sentinel");
        harness.assertInGraveyard(player1, "Axgard Cavalry");
    }

    @Test
    void chapterIIPutsCountersOnEachElfYouControl() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        Permanent elf = addCreatureReady(player1, new JasperaSentinel());
        Permanent nonElf = addCreatureReady(player1, new AxgardCavalry());
        Permanent opposingElf = addCreatureReady(player2, new JasperaSentinel());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonElf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingElf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chapterIIITargetsAnOpposingCreatureForEachElfThatAttacks() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 2);
        addCreatureReady(player1, new JasperaSentinel());
        addCreatureReady(player1, new AxgardCavalry());
        Permanent opponentCreature = addCreatureReady(player2, new AxgardCavalry());

        advanceToNextChapter();
        harness.passBothPriorities();

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, opponentCreature)).isEqualTo(-1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentCreature)).isEqualTo(-1);
    }

    @Test
    void chapterIIIOnlyTriggersForElvesYouControl() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent opposingElf = addCreatureReady(player2, new JasperaSentinel());
        addCreatureReady(player2, new AxgardCavalry());

        advanceToNextChapter();
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(PermanentGrants.powerBoostOf(gd, opposingElf)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opposingElf)).isZero();
    }

    @Test
    void chapterICanReturnTyvarFromYourGraveyard() {
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new SnowCoveredIsland(),
                new SnowCoveredIsland(), new SnowCoveredIsland()));
        harness.setGraveyard(player1, List.of(new TyvarKell(), new AxgardCavalry()));
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Tyvar Kell");
        harness.assertNotInGraveyard(player1, "Tyvar Kell");
        harness.assertInGraveyard(player1, "Axgard Cavalry");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void chapterICanReturnAnElfMilledByTheSameChapter() {
        harness.setLibrary(player1, List.of(new JasperaSentinel(), new SnowCoveredIsland(),
                new SnowCoveredIsland(), new SnowCoveredIsland()));
        harness.setGraveyard(player1, List.of());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Jaspera Sentinel");
        harness.assertNotInGraveyard(player1, "Jaspera Sentinel");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void chapterIStillMillsWhenYouDeclineTheReturn() {
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new SnowCoveredIsland(),
                new SnowCoveredIsland(), new SnowCoveredIsland()));
        harness.setGraveyard(player1, List.of(new JasperaSentinel()));
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Jaspera Sentinel");
        harness.assertInGraveyard(player1, "Jaspera Sentinel");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void chapterIIICreatesASeparateTriggerForEachAttackingElfAfterSagaIsSacrificed() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 2);
        addCreatureReady(player1, new JasperaSentinel());
        addCreatureReady(player1, new JasperaSentinel());
        Permanent firstTarget = addCreatureReady(player2, new AxgardCavalry());
        Permanent secondTarget = addCreatureReady(player2, new AxgardCavalry());

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Harald Unites the Elves");
        harness.assertInGraveyard(player1, "Harald Unites the Elves");

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, firstTarget.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, firstTarget)).isEqualTo(-1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstTarget)).isEqualTo(-1);
        assertThat(PermanentGrants.powerBoostOf(gd, secondTarget)).isEqualTo(-1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondTarget)).isEqualTo(-1);
    }
    @Test
    void chapterIIITriggerAndDebuffExpireAtEndOfTurn() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HaraldUnitesTheElves());
        saga.setCounterCount(CounterType.LORE, 2);
        addCreatureReady(player1, new JasperaSentinel());
        Permanent target = addCreatureReady(player2, new AxgardCavalry());

        advanceToNextChapter();
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(-1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(-1);
        gs.declareBlockers(gd, player2, List.of());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.performUntapStep(player1);

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
    }
    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
