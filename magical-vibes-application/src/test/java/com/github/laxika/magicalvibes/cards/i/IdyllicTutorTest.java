package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.m.MudbuttonClanger;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IdyllicTutor.class, Bitterblossom.class, ElvishWarrior.class, MudbuttonClanger.class})
class IdyllicTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving presents only enchantment cards for the search")
    void resolvingPresentsOnlyEnchantments() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .isNotEmpty()
                .allMatch(c -> c.hasType(CardType.ENCHANTMENT));
    }

    @Test
    @DisplayName("Chosen enchantment goes to hand, is revealed, and library is shuffled")
    void chosenEnchantmentGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.hasType(CardType.ENCHANTMENT));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("No prompt is created when the library holds no enchantment")
    void noEnchantmentInLibrary() {
        setupAndCast();

        harness.setLibrary(player1, List.of(new ElvishWarrior(), new MudbuttonClanger()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("finds no enchantment cards")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("May fail to find even when an enchantment is available, then shuffles")
    void mayFailToFindWithEnchantmentAvailable() {
        setupAndCast();
        Bitterblossom enchantment = new Bitterblossom();
        ElvishWarrior creature = new ElvishWarrior();
        harness.setLibrary(player1, List.of(enchantment, creature));
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(enchantment, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals")).isFalse();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("An empty library finishes the search and is shuffled")
    void emptyLibraryFinishesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new IdyllicTutor(), "{2}{W}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new ElvishWarrior(), new Bitterblossom(), new MudbuttonClanger()));
    }
}
