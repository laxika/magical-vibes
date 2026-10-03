package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurningRuneDemon.class, SnowCoveredIsland.class, SnowCoveredSwamp.class})
class BurningRuneDemonTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses which of two differently named cards goes to hand")
    void opponentChoosesCardForHand() {
        Card handCard = new SnowCoveredSwamp();
        Card duplicateName = new SnowCoveredSwamp();
        Card graveyardCard = new SnowCoveredIsland();
        Card excludedCard = new BurningRuneDemon();
        harness.setLibrary(player1, List.of(handCard, duplicateName, graveyardCard, excludedCard));

        castAndResolveMay(true);

        PendingInteraction.IntuitionSearchChoice search =
                gd.interaction.activeInteraction(PendingInteraction.IntuitionSearchChoice.class);
        assertThat(search.count()).isEqualTo(2);
        assertThat(search.requireDifferentNames()).isTrue();
        assertThat(search.pool()).containsExactly(handCard, duplicateName, graveyardCard);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(handCard.getId(), duplicateName.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different names");

        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId(), graveyardCard.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultipleCardsChosen(player2, List.of(graveyardCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(duplicateName, excludedCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB search leaves the library unchanged")
    void decliningSearchDoesNothing() {
        Card first = new SnowCoveredSwamp();
        Card second = new SnowCoveredIsland();
        harness.setLibrary(player1, List.of(first, second));

        castAndResolveMay(false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search cannot resolve when fewer than two eligible names exist")
    void fewerThanTwoEligibleNamesFindsNothing() {
        Card eligible = new SnowCoveredSwamp();
        Card excluded = new BurningRuneDemon();
        harness.setLibrary(player1, List.of(eligible, excluded));

        castAndResolveMay(true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(eligible, excluded);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eligible, excluded);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(eligible, excluded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A restricted library search may fail to find even when eligible cards exist")
    void mayFindNothingWithTwoEligibleNames() {
        Card first = new SnowCoveredSwamp();
        Card second = new SnowCoveredIsland();
        harness.setLibrary(player1, List.of(first, second));

        castAndResolveMay(true);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Two copies of one eligible name do not satisfy the search")
    void duplicateEligibleNamesFindNothing() {
        Card first = new SnowCoveredSwamp();
        Card second = new SnowCoveredSwamp();
        harness.setLibrary(player1, List.of(first, second));

        castAndResolveMay(true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolveMay(boolean accept) {
        harness.castFromHand(player1, new BurningRuneDemon(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
