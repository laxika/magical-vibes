package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerchantOfSecrets.class, Forest.class})
class MerchantOfSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void etbDrawsACard() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.castFromHand(player1, new MerchantOfSecrets(), "{2}{U}");
        resolveAllTriggers();

        // Cast Merchant (hand -1), then drew 1 → net 0
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Drawing waits for the enter-the-battlefield trigger to resolve")
    void drawWaitsForTriggerResolution() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castFromHand(player1, new MerchantOfSecrets(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Merchant of Secrets");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Entering without being cast draws only for the creature's controller")
    void enteringWithoutCastingDrawsForController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player2, new MerchantOfSecrets());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Merchant of Secrets");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }
}
