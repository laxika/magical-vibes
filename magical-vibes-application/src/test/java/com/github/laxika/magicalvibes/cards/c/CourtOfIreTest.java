package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.q.QueenMarchesa;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourtOfIre.class, QueenMarchesa.class})
class CourtOfIreTest extends BaseCardTest {

    @Test
    void entersAndMakesItsControllerTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfIre());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void dealsSevenDamageDuringUpkeepWhileItsControllerIsTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfIre());
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void dealsTwoDamageDuringUpkeepWhenAnOpponentIsTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfIre());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player2, new QueenMarchesa());
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsTwoDamageIfItsControllerLosesTheMonarchyBeforeResolution() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfIre());
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.enterBattlefieldAndReturn(player2, new CourtOfIre());
        harness.passBothPriorities();
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsSevenDamageIfItsControllerGainsTheMonarchyBeforeResolution() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfIre());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player2, new CourtOfIre());
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.enterBattlefieldAndReturn(player1, new CourtOfIre());
        harness.passBothPriorities();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void doesNotTriggerDuringAnOpponentsUpkeep() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfIre());
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canDealLethalDamageToACreatureWhileItsControllerIsTheMonarch() {
        var creature = harness.addToBattlefieldAndReturn(player2, new QueenMarchesa());
        harness.enterBattlefieldAndReturn(player1, new CourtOfIre());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    void dealsTwoNonlethalDamageToACreatureWhenItsControllerIsNotTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfIre());
        harness.passBothPriorities();
        var creature = harness.enterBattlefieldAndReturn(player2, new QueenMarchesa());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }
}
