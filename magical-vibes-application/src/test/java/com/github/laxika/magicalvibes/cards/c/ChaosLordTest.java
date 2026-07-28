package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChaosLordTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger only offers opponents as targets")
    void upkeepTriggerOnlyTargetsOpponents() {
        harness.addToBattlefield(player1, new ChaosLord());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Target opponent gains control when the number of permanents is even")
    void opponentGainsControlOnEvenPermanentCount() {
        harness.addToBattlefield(player1, new ChaosLord());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chaos Lord");
        harness.assertOnBattlefield(player2, "Chaos Lord");
    }

    @Test
    @DisplayName("Control does not change when the number of permanents is odd")
    void controlStaysOnOddPermanentCount() {
        harness.addToBattlefield(player1, new ChaosLord());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chaos Lord");
        harness.assertNotOnBattlefield(player2, "Chaos Lord");
    }

    @Test
    @DisplayName("Can attack while summoning sick if it did not enter the battlefield this turn")
    void attacksDespiteSummoningSickness() {
        harness.addToBattlefield(player1, new ChaosLord());
        // A blocker on the defending side so combat pauses at declare-blockers (isAttacking stays set).
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(findPermanent(player1, "Chaos Lord").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot attack on the turn it entered the battlefield")
    void cannotAttackOnTheTurnItEntered() {
        harness.setHand(player1, List.of(new ChaosLord()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
