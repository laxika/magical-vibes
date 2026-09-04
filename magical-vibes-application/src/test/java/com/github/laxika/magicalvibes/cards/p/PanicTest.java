package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.j.JestersCap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Panic.class, PaleBears.class, JestersCap.class})
class PanicTest extends BaseCardTest {

    @Test
    @DisplayName("Cast during declare attackers: target can't block and a draw is scheduled")
    void targetCantBlockAndSchedulesDraw() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addCreatureReady(player2, new PaleBears());
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addCreatureReady(player2, new PaleBears());
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Targeted creature actually cannot block")
    void targetedCreatureCannotBlock() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new PaleBears());
        Permanent blocker = addCreatureReady(player2, new PaleBears());
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast once blockers are declared")
    void cannotCastDuringDeclareBlockers() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addCreatureReady(player2, new PaleBears());
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside combat")
    void cannotCastOutsideCombat() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addCreatureReady(player2, new PaleBears());
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        addCreatureReady(player2, new PaleBears()); // valid target so spell is playable
        harness.addToBattlefield(player2, new JestersCap());
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        UUID capId = harness.getPermanentId(player2, "Jester's Cap");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, capId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can be cast during beginning of combat by either player")
    void canBeCastDuringBeginningOfCombatByEitherPlayer() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player1, new PaleBears());
        harness.setHand(player2, List.of(new Panic()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The restriction ends during cleanup")
    void cantBlockRestrictionEndsDuringCleanup() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addCreatureReady(player2, new PaleBears());
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Does not draw during an additional upkeep in the same turn")
    void drawWaitsForNextTurnWhenAdditionalUpkeepOccurs() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addCreatureReady(player2, new PaleBears());
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        gd.additionalUpkeepStepsAfterCombat = 1;
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
