package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurningRuneDemonTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses which of two differently named cards goes to hand")
    void opponentChoosesCardForHand() {
        Card handCard = new Shock();
        Card duplicateName = new Shock();
        Card graveyardCard = new Island();
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
        Card first = new Shock();
        Card second = new Island();
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
        Card eligible = new Shock();
        Card excluded = new BurningRuneDemon();
        harness.setLibrary(player1, List.of(eligible, excluded));

        castAndResolveMay(true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(eligible, excluded);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eligible, excluded);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(eligible, excluded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolveMay(boolean accept) {
        harness.setHand(player1, List.of(new BurningRuneDemon()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
