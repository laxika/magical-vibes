package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PleaForGuidance.class, Pacifism.class, GrizzlyBears.class})
class PleaForGuidanceTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to two revealed enchantment cards")
    void searchesForUpToTwoEnchantments() {
        cast();
        harness.setLibrary(player1, List.of(new Pacifism(), new Pacifism(), new Pacifism(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3)
                .allMatch(card -> card.hasType(CardType.ENCHANTMENT));
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Pacifism", "Pacifism");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Pacifism", "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not prompt when the library has no enchantments")
    void noEnchantmentsInLibrary() {
        cast();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May find zero cards even when enchantments are available")
    void mayChooseZeroCards() {
        cast();
        Pacifism enchantment = new Pacifism();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(enchantment, creature));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(enchantment, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May stop after finding one enchantment when another is available")
    void mayStopAfterOneCard() {
        cast();
        Pacifism chosen = new Pacifism();
        Pacifism remaining = new Pacifism();
        harness.setLibrary(player1, List.of(chosen, remaining));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Finishes after finding the only enchantment in the library")
    void finishesWhenNoMoreEnchantmentsRemain() {
        cast();
        Pacifism enchantment = new Pacifism();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(enchantment, creature));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves with an empty library")
    void emptyLibrary() {
        cast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void cast() {
        harness.setHand(player1, List.of(new PleaForGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0, 0);
    }
}
