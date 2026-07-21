package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SanctumPlowbeastTest extends BaseCardTest {

    // ===== Plainscycling {2} =====

    @Test
    @DisplayName("Plainscycling discards the card and offers only Plains cards")
    void plainscyclingDiscardsAndOffersPlains() {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanctum Plowbeast");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Plains"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Choosing a Plains from the search puts it into hand")
    void choosingPlainsPutsItIntoHand() {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Plains"));
    }

    // ===== Islandcycling {2} =====

    @Test
    @DisplayName("Islandcycling discards the card and offers only Island cards")
    void islandcyclingDiscardsAndOffersIslands() {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.ensurePriority(player1);
        harness.getGameService().activateHandAbility(gd, player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanctum Plowbeast");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Island"))
                .hasSize(1);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Plains(), new Island(),
                new Forest(), new GrizzlyBears()));
    }
}
