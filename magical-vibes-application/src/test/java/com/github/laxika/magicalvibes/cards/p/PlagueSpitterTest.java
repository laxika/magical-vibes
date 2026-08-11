package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlagueSpitterTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger deals 1 damage to each creature and each player")
    void upkeepTriggerDealsOneDamageToAllCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PlagueSpitter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Plague Spitter").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player1, "Grizzly Bears").getFirst().getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player2, "Grizzly Bears").getFirst().getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("When Plague Spitter dies, it deals 1 damage to each creature and each player")
    void deathTriggerDealsOneDamageToAllCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new PlagueSpitter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        spitter.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plague Spitter");
        assertThat(findPermanents(player1, "Grizzly Bears").getFirst().getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player2, "Grizzly Bears").getFirst().getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire on the opponent's upkeep")
    void upkeepTriggerDoesNotFireOnOpponentsUpkeep() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PlagueSpitter());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Plague Spitter").getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
