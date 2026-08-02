package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KezzerdrixTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to its controller at upkeep when opponents control no creatures")
    void damagesControllerWhenOpponentHasNoCreatures() {
        harness.addToBattlefield(player1, new Kezzerdrix());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 4);
    }

    @Test
    @DisplayName("Does not damage its controller while an opponent controls a creature")
    void noDamageWhenOpponentControlsCreature() {
        harness.addToBattlefield(player1, new Kezzerdrix());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Controller's own creatures do not stop the trigger")
    void controllerCreaturesDoNotStopTrigger() {
        harness.addToBattlefield(player1, new Kezzerdrix());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 4);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Kezzerdrix());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }
}
