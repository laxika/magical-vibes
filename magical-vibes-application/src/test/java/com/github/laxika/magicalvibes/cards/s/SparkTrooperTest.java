package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SparkTrooperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacks immediately with haste, dealing 6 and gaining 6 life from lifelink")
    void attacksImmediatelyAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent trooper = new Permanent(new SparkTrooper());
        trooper.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(trooper);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Sacrifices itself at the beginning of the end step")
    void sacrificesItselfAtEndStep() {
        Permanent trooper = new Permanent(new SparkTrooper());
        gd.playerBattlefields.get(player1.getId()).add(trooper);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(trooper.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spark Trooper");
        harness.assertInGraveyard(player1, "Spark Trooper");
    }
}
