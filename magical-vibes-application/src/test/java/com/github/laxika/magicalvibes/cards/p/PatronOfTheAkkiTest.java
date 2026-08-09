package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatronOfTheAkkiTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives creatures you control +2/+0 until end of turn")
    void attackBoostsControlledCreatures() {
        Permanent patron = addCreatureReady(player1, new PatronOfTheAkki());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, patron)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent patron = addCreatureReady(player1, new PatronOfTheAkki());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, patron)).isEqualTo(5);
    }

    @Test
    @DisplayName("Offering sacrifices a Goblin and reduces matching colored mana")
    void offeringSacrificesGoblinAndReducesColoredMana() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        harness.setHand(player1, List.of(new PatronOfTheAkki()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(goblin.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Akki");
        harness.assertNotOnBattlefield(player1, "Goblin Piker");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
