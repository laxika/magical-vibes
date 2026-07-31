package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GorillaWarCryTest extends BaseCardTest {

    @Test
    @DisplayName("Every creature on the battlefield gains menace until end of turn")
    void allCreaturesGainMenace() {
        harness.forceActivePlayer(player1);
        Permanent mine = addCreatureReady(player1, new GrizzlyBears());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(mine.getGrantedKeywords()).contains(Keyword.MENACE);
        assertThat(theirs.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("A single blocker cannot block an attacker that gained menace")
    void singleBlockerIsIllegal() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two blockers can still block the menacing attacker")
    void twoBlockersAreLegal() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.combatBlockOpponentIdsThisTurn.get(attacker.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.forceActivePlayer(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast once blockers are declared")
    void cannotCastDuringDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside combat")
    void cannotCastOutsideCombat() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
