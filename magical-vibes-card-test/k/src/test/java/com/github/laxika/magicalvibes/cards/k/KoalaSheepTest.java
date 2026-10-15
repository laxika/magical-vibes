package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KoalaSheep.class)
class KoalaSheepTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains its controller 3 life")
    void enteringBattlefieldGainsThreeLife() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KoalaSheep()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Life is gained only when the enter trigger resolves")
    void lifeGainWaitsForTriggerResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new KoalaSheep()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Koala-Sheep");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 23);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second player's Koala-Sheep gains life for that player only")
    void secondPlayerGainsLifeForTheirCreature() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new KoalaSheep()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
    }
}
