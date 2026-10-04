package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GlimpseTheUnthinkable.class)
class GlimpseTheUnthinkableTest extends BaseCardTest {

    @Test
    @DisplayName("Mills ten cards from target player's library")
    void millsTenCards() {
        harness.setHand(player1, List.of(new GlimpseTheUnthinkable()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.setLibrary(player2, glimpseLibrary(10));

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
    }

    @Test
    @DisplayName("Can target its controller and mills exactly ten cards")
    void canTargetControllerAndMillsExactlyTenCards() {
        harness.setHand(player1, List.of(new GlimpseTheUnthinkable()));
        harness.setLibrary(player1, glimpseLibrary(11));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(11);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library has fewer than ten")
    void millsOnlyRemainingWhenLibrarySmall() {
        harness.setHand(player1, List.of(new GlimpseTheUnthinkable()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.setLibrary(player2, glimpseLibrary(3));

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills the top ten cards and leaves the other player's library untouched")
    void millsOnlyTopTenOfTargetLibrary() {
        List<Card> targetLibrary = glimpseLibrary(12);
        List<Card> controllerLibrary = glimpseLibrary(4);
        harness.setLibrary(player2, targetLibrary);
        harness.setLibrary(player1, controllerLibrary);
        harness.setHand(player1, List.of(new GlimpseTheUnthinkable()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrderElementsOf(targetLibrary.subList(0, 10));
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyElementsOf(targetLibrary.subList(10, 12));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(controllerLibrary);
    }

    @Test
    @DisplayName("Can resolve targeting an empty library without causing a loss")
    void resolvesWithEmptyTargetLibrary() {
        harness.setHand(player1, List.of(new GlimpseTheUnthinkable()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Glimpse the Unthinkable");
    }

    private List<Card> glimpseLibrary(int size) {
        return IntStream.range(0, size)
                .mapToObj(ignored -> (Card) new GlimpseTheUnthinkable())
                .toList();
    }
}
