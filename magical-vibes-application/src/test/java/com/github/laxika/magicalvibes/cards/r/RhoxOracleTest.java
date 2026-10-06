package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhoxOracle.class, Forest.class, Murder.class})
class RhoxOracleTest extends BaseCardTest {

    @Test
    @DisplayName("When Rhox Oracle enters, its controller draws a card")
    void drawsCardOnEnter() {
        harness.setHand(player1, List.of(new RhoxOracle()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Rhox Oracle");
    }

    @Test
    @DisplayName("Rhox Oracle draws only when its enters trigger resolves")
    void drawWaitsForTriggerResolution() {
        harness.setHand(player1, List.of(new RhoxOracle()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rhox Oracle");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rhox Oracle's enters trigger draws even if it is destroyed in response")
    void drawsAfterSourceIsDestroyed() {
        harness.setHand(player1, List.of(new RhoxOracle()));
        harness.setHand(player2, List.of(new Murder()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0,
                findPermanent(player1, "Rhox Oracle").getId());

        harness.assertInGraveyard(player1, "Rhox Oracle");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
