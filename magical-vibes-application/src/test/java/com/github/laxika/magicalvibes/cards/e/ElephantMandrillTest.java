package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElephantMandrill.class, FountainOfYouth.class})
class ElephantMandrillTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each player creates a Food token")
    void eachPlayerCreatesFoodOnEntry() {
        castElephantMandrill();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player2, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("At the beginning of combat, gets +1/+1 for each artifact opponents control")
    void boostsForOpponentArtifacts() {
        Permanent elephantMandrill = harness.addToBattlefieldAndReturn(player1, new ElephantMandrill());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elephantMandrill)).isEqualTo(4);
    }

    @Test
    @DisplayName("The combat boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent elephantMandrill = harness.addToBattlefieldAndReturn(player1, new ElephantMandrill());
        harness.addToBattlefield(player2, new FountainOfYouth());

        advanceToCombatAndResolve(player1);
        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elephantMandrill)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephantMandrill)).isEqualTo(2);
    }

    private void castElephantMandrill() {
        harness.setHand(player1, List.of(new ElephantMandrill()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
