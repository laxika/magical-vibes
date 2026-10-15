package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SafewrightQuest.class, Forest.class, Plains.class, SafeholdElite.class, TempleGarden.class})
class SafewrightQuestTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving presents only Forest and Plains cards")
    void presentsForestAndPlains() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"))
                .isNotEmpty();
    }

    @Test
    @DisplayName("Chosen card goes to hand")
    void chosenCardGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Plains is revealed and moved from library to hand, then library is shuffled")
    void findsPlains() {
        Plains plains = new Plains();
        Forest forest = new Forest();
        setupAndCast();
        harness.setLibrary(player1, List.of(forest, plains));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("reveals Plains")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        harness.assertInGraveyard(player1, "Safewright Quest");
    }

    @Test
    @DisplayName("A nonbasic Forest Plains card can be found and counts as a single card")
    void findsNonbasicLand() {
        TempleGarden garden = new TempleGarden();
        setupAndCast();
        harness.setLibrary(player1, List.of(garden, new SafeholdElite()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(garden);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(garden);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(garden).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("reveals Temple Garden")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("May fail to find even when eligible lands are present")
    void mayFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        harness.assertInGraveyard(player1, "Safewright Quest");
    }

    @Test
    @DisplayName("A library without matching cards resolves without a choice")
    void noMatchingCards() {
        SafeholdElite creature = new SafeholdElite();
        setupAndCast();
        harness.setLibrary(player1, List.of(creature));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        harness.assertInGraveyard(player1, "Safewright Quest");
    }

    @Test
    @DisplayName("An empty library resolves without a choice")
    void emptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        harness.assertInGraveyard(player1, "Safewright Quest");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new SafewrightQuest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new SafeholdElite()));
    }
}
