package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampireSpawn.class})
class VampireSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each opponent lose 2 life and its controller gain 2 life")
    void entersBattlefieldDrainsOpponentsAndGainsLife() {
        castVampireSpawn();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB trigger is put on the stack after the creature resolves")
    void entersBattlefieldPutsTriggerOnStack() {
        castVampireSpawn();

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    private void castVampireSpawn() {
        harness.castFromHand(player1, new VampireSpawn(), "{2}{B}");
    }
    @Test
    @DisplayName("When it enters, each opponent loses 2 life and you gain 2 life")
    void enteringDrainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        castVampireSpawn();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Life totals change only when the enters trigger resolves")
    void lifeTotalsRemainUnchangedUntilTriggerResolves() {
        castVampireSpawn();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vampire Spawn");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The other player's Vampire Spawn gains life for its own controller")
    void otherControllerGainsLifeAndDrainsTheirOpponent() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new VampireSpawn(), "{2}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }
}
