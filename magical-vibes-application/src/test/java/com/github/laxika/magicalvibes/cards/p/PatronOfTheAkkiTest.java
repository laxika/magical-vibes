package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatronOfTheAkki.class, GnarledMass.class, GoblinCohort.class})
class PatronOfTheAkkiTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives creatures you control +2/+0 until end of turn")
    void attackBoostsControlledCreatures() {
        Permanent patron = addCreatureReady(player1, new PatronOfTheAkki());
        Permanent mass = addCreatureReady(player1, new GnarledMass());
        Permanent opposingMass = addCreatureReady(player2, new GnarledMass());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, patron)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingMass)).isEqualTo(3);
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
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinCohort());
        harness.setHand(player1, List.of(new PatronOfTheAkki()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(goblin.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Akki");
        harness.assertNotOnBattlefield(player1, "Goblin Cohort");
        harness.assertInGraveyard(player1, "Goblin Cohort");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Offering can cast Patron of the Akki at instant speed")
    void offeringCanBeCastAtInstantSpeed() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinCohort());
        harness.setHand(player1, List.of(new PatronOfTheAkki()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.castCreatureWithAlternateCost(player1, 0, List.of(goblin.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Akki");
    }

    @Test
    @DisplayName("Offering cannot sacrifice a non-Goblin creature")
    void offeringRequiresGoblin() {
        Permanent mass = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        harness.setHand(player1, List.of(new PatronOfTheAkki()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() ->
                        harness.castCreatureWithAlternateCost(player1, 0, List.of(mass.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
