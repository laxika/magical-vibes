package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WailingGhoul.class, Forest.class})
class WailingGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills two cards from its controller's library")
    void etbMillsTwoCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.setHand(player1, List.of(new WailingGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB mills its controller, not an opponent")
    void etbMillsControllerNotOpponent() {
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardSize = gd.playerGraveyards.get(player2.getId()).size();

        harness.setHand(player1, List.of(new WailingGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardSize);
    }

    @Test
    @DisplayName("ETB mills only the cards remaining in a short library")
    void etbMillsOnlyRemainingCards() {
        harness.setLibrary(player1, List.of(new Forest()));

        harness.setHand(player1, List.of(new WailingGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB mills exactly the top two cards and leaves the rest in order")
    void etbMillsTopTwoCards() {
        WailingGhoul first = new WailingGhoul();
        WailingGhoul second = new WailingGhoul();
        WailingGhoul third = new WailingGhoul();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new WailingGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertOnBattlefield(player1, "Wailing Ghoul");
    }

    @Test
    @DisplayName("ETB with an empty library does not cause a loss")
    void etbWithEmptyLibraryDoesNotCauseLoss() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new WailingGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
        harness.assertOnBattlefield(player1, "Wailing Ghoul");
    }
}
