package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcRunner.class})
class ArcRunnerTest extends BaseCardTest {

    

    @Test
    @DisplayName("Can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent arcRunner = harness.addToBattlefieldAndReturn(player1, new ArcRunner());
        arcRunner.setSummoningSick(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Triggers at end step and sacrifices itself on resolution")
    void triggersAtEndStepAndSacrificesItself() {
        Permanent arcRunner = harness.addToBattlefieldAndReturn(player1, new ArcRunner());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Arc Runner");
        assertThat(trigger.getSourcePermanentId()).isEqualTo(arcRunner.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arc Runner");
        harness.assertInGraveyard(player1, "Arc Runner");
    }

    @Test
    @DisplayName("Sacrifices itself at the opponent's end step too")
    void sacrificesAtOpponentsEndStep() {
        Permanent arcRunner = harness.addToBattlefieldAndReturn(player1, new ArcRunner());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(arcRunner.getId());
        harness.assertOnBattlefield(player1, "Arc Runner");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arc Runner");
        harness.assertInGraveyard(player1, "Arc Runner");
    }

    @Test
    @DisplayName("Each copy sacrifices only itself when its trigger resolves")
    void eachCopySacrificesOnlyItself() {
        harness.addToBattlefield(player1, new ArcRunner());
        harness.addToBattlefield(player1, new ArcRunner());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        var firstSourceId = gd.stack.getFirst().getSourcePermanentId();
        var topSourceId = gd.stack.getLast().getSourcePermanentId();
        assertThat(topSourceId).isNotEqualTo(firstSourceId);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).containsExactly(firstSourceId);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arc Runner");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
