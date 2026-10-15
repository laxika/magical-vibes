package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AethermagesTouch;
import com.github.laxika.magicalvibes.cards.a.AzoriusHerald;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalTutor.class, AethermagesTouch.class, AzoriusHerald.class})
class InfernalTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals a card from hand and searches for a card with the same name")
    void searchesForSameNameAsCardInHand() {
        harness.setHand(player1, List.of(new InfernalTutor(), new AethermagesTouch()));
        harness.setLibrary(player1, List.of(new AzoriusHerald(), new AethermagesTouch()));
        addManaForInfernalTutor();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice handChoice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(handChoice.options()).containsExactly("Aethermage's Touch");

        harness.handleListChoice(player1, "Aethermage's Touch");

        PendingInteraction.LibrarySearch search =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Aethermage's Touch");
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Aethermage's Touch", "Aethermage's Touch");
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    @Test
    @DisplayName("Searches for any card when the controller has no cards in hand")
    void searchesForAnyCardWithEmptyHand() {
        harness.setLibrary(player1, List.of(new AethermagesTouch()));
        harness.castFromHand(player1, new InfernalTutor(), "{1}{B}");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Aethermage's Touch");
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Aethermage's Touch");
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    @Test
    @DisplayName("Completes without finding a card when the chosen name is absent from the library")
    void completesWhenSameNameCardIsAbsent() {
        harness.setHand(player1, List.of(new InfernalTutor(), new AethermagesTouch()));
        harness.setLibrary(player1, List.of(new AzoriusHerald()));
        addManaForInfernalTutor();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Aethermage's Touch");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Aethermage's Touch");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Azorius Herald");
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    @Test
    @DisplayName("Can reveal either card in hand and searches only for the chosen name")
    void choosesAmongCardsInHand() {
        harness.setHand(player1, List.of(new InfernalTutor(), new AethermagesTouch(), new AzoriusHerald()));
        harness.setLibrary(player1, List.of(new AethermagesTouch(), new AzoriusHerald()));
        addManaForInfernalTutor();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder("Aethermage's Touch", "Azorius Herald");
        harness.handleListChoice(player1, "Azorius Herald");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Azorius Herald");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Aethermage's Touch", "Azorius Herald", "Azorius Herald");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Aethermage's Touch");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May fail to find even when a card with the revealed name exists")
    void mayFailToFindMatchingCard() {
        harness.setHand(player1, List.of(new InfernalTutor(), new AethermagesTouch()));
        harness.setLibrary(player1, List.of(new AethermagesTouch()));
        addManaForInfernalTutor();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Aethermage's Touch");
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Aethermage's Touch");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Aethermage's Touch");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Checks hellbent at resolution rather than when the spell is cast")
    void usesHandAtResolution() {
        harness.setLibrary(player1, List.of(new AethermagesTouch(), new AzoriusHerald()));
        harness.castFromHand(player1, new InfernalTutor(), "{1}{B}");
        harness.setHand(player1, List.of(new AzoriusHerald()));

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Azorius Herald");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Azorius Herald");
        assertThat(search.params().reveals()).isTrue();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Azorius Herald", "Azorius Herald");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not leave the hellbent search waiting for input")
    void hellbentWithEmptyLibraryCompletes() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new InfernalTutor(), "{1}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    private void addManaForInfernalTutor() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
