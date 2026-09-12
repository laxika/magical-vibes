package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Blastoderm;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.s.SealOfCleansing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PackHunt.class, Mossdog.class, Blastoderm.class, SealOfCleansing.class})
class PackHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to three cards with the target creature's name")
    void searchesForCardsWithTargetName() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Mossdog()).getId();
        harness.setLibrary(player1, List.of(new Mossdog(), new Mossdog(), new Mossdog(), new Blastoderm()));

        cast(targetId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Mossdog", "Mossdog", "Mossdog");
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mossdog", "Mossdog", "Mossdog");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Blastoderm");
    }

    @Test
    @DisplayName("Can choose fewer than three matching cards")
    void canChooseFewerThanThreeMatchingCards() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Mossdog()).getId();
        harness.setLibrary(player1, List.of(new Mossdog(), new Mossdog(), new Blastoderm()));

        cast(targetId);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mossdog");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Mossdog", "Blastoderm");
    }

    @Test
    @DisplayName("Resolves without a choice when the library has no matching cards")
    void resolvesWhenNoMatchingCardsAreFound() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Mossdog()).getId();
        harness.setLibrary(player1, List.of(new Blastoderm()));

        cast(targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Blastoderm");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new SealOfCleansing()).getId();

        assertThatThrownBy(() -> cast(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new PackHunt()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, targetId);
    }
}
