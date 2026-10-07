package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimbermawLarva.class, Forest.class, Island.class})
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
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, larva)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, larva)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts Forests at resolution and does not recalculate the resolved boost")
    void countsForestsWhenTriggerResolves() {
        Permanent larva = addCreatureReady(player1, new TimbermawLarva());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, larva)).isEqualTo(2);
        harness.addToBattlefield(player1, new Forest());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, larva)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, larva)).isEqualTo(4);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, larva)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, larva)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only the Larva that attacks gets the boost")
    void doesNotBoostNonattackingLarvas() {
        Permanent attacker = addCreatureReady(player1, new TimbermawLarva());
        Permanent nonattacker = addCreatureReady(player1, new TimbermawLarva());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonattacker)).isEqualTo(2);
    }
}
