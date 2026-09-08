package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimbermawLarvaTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each Forest you control when it attacks")
    void boostsForControlledForests() {
        Permanent larva = addCreatureReady(player1, new TimbermawLarva());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, larva)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, larva)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count an opponent's Forests or your non-Forest lands")
    void countsOnlyControlledForests() {
        Permanent larva = addCreatureReady(player1, new TimbermawLarva());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, larva)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, larva)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost lasts until end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent larva = addCreatureReady(player1, new TimbermawLarva());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, larva)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, larva)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, larva)).isEqualTo(2);
    }
}
