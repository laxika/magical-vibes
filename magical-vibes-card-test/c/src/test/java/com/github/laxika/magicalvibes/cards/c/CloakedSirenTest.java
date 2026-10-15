package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PensiveMinotaur;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloakedSiren.class, PensiveMinotaur.class})
class CloakedSirenTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during an opponent's turn thanks to flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.passPriority(gd, player2);
        harness.castFromHand(player1, new CloakedSiren(), "{3}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent siren = harness.addToBattlefieldAndReturn(player1, new CloakedSiren());
        siren.setSummoningSick(false);
        harness.addToBattlefield(player2, new PensiveMinotaur());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can flash in during the opponent's end step and resolve")
    void canCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.passPriority(gd, player2);

        harness.castFromHand(player1, new CloakedSiren(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloaked Siren");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast in response to another creature spell")
    void canCastWithSpellOnStack() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new PensiveMinotaur(), "{2}{R}");
        gs.passPriority(gd, player2);

        harness.castFromHand(player1, new CloakedSiren(), "{3}{U}");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloaked Siren");
        harness.assertNotOnBattlefield(player2, "Pensive Minotaur");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Another flying creature can block Cloaked Siren even while summoning sick")
    void flyingCreatureCanBlock() {
        Permanent siren = harness.addToBattlefieldAndReturn(player1, new CloakedSiren());
        siren.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CloakedSiren());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getBlockingTargets()).containsExactly(0);
    }
}
