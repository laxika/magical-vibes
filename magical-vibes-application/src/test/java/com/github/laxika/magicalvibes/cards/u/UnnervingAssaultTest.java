package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnnervingAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("{U} spent: opponents' creatures get -1/-0, your creatures unaffected")
    void blueSpentShrinksOpponents() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());   // 2/2
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new UnnervingAssault()));
        harness.addMana(player1, ManaColor.BLUE, 3); // {2}{U/R} all blue → only {U} spent
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
        assertThat(mine.getEffectivePower()).isEqualTo(2);
        assertThat(mine.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("{R} spent: your creatures get +1/+0, opponents' unaffected")
    void redSpentPumpsYours() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());   // 2/2
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new UnnervingAssault()));
        harness.addMana(player1, ManaColor.RED, 3); // {2}{U/R} all red → only {R} spent
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(3);
        assertThat(mine.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("{U}{R} spent: both clauses apply")
    void bothColorsApplyBothClauses() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());   // 2/2
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new UnnervingAssault()));
        // Surplus of both colors so the {U/R} hybrid pip and the {2} generic are paid across
        // both colors — both {U} and {R} are spent.
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(mine.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new UnnervingAssault()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(mine.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(2);
    }
}
