package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(JuzMDjinn.class)
class JuzMDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to its controller at the beginning of their upkeep")
    void dealsOneDamageToControllerAtUpkeep() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new JuzMDjinn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new JuzMDjinn());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Djinn deals damage independently during its controller's upkeep")
    void multipleDjinnsEachDealDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new JuzMDjinn());
        harness.addToBattlefield(player1, new JuzMDjinn());
        harness.addToBattlefield(player2, new JuzMDjinn());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals damage to the second player during their upkeep")
    void damagesSecondPlayerOnTheirUpkeep() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new JuzMDjinn());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An upkeep ability still deals damage after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        var djinn = harness.addToBattlefieldAndReturn(player1, new JuzMDjinn());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        gd.playerBattlefields.get(player1.getId()).remove(djinn);
        gd.playerGraveyards.get(player1.getId()).add(djinn.getCard());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }
}
