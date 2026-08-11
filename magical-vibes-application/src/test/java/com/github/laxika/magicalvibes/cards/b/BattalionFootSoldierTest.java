package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BattalionFootSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Battalion Foot Soldier creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability searches for every matching card in the library")
    void acceptingSearchesForMatchingCards() {
        setupAndCast();
        setupLibraryWithSoldiers(3);

        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(3)
                .allMatch(card -> card instanceof BattalionFootSoldier);
    }

    @Test
    @DisplayName("Selecting all matching cards puts them into hand")
    void selectingAllMatchingCardsPutsThemIntoHand() {
        setupAndCast();
        setupLibraryWithSoldiers(3);
        resolveMayAbility(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        for (int i = 0; i < 3; i++) {
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card ->
                card instanceof BattalionFootSoldier).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The search may find no cards")
    void searchMayFindNoCards() {
        setupAndCast();
        setupLibraryWithSoldiers(2);
        resolveMayAbility(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new BattalionFootSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveMayAbility(boolean choice) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private void setupLibraryWithSoldiers(int soldierCount) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        for (int i = 0; i < soldierCount; i++) {
            deck.add(new BattalionFootSoldier());
        }
        deck.add(new GrizzlyBears());
    }
}
