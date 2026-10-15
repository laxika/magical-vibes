package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValleyRannet.class, Forest.class, Mountain.class, Swamp.class, GrizzlyBears.class})
class ValleyRannetTest extends BaseCardTest {

    @Test
    @DisplayName("Mountaincycling discards the card and offers only Mountain cards")
    void mountaincyclingDiscardsAndOffersMountains() {
        harness.setHand(player1, List.of(new ValleyRannet()));
        harness.addMana(player1, ManaColor.RED, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Valley Rannet");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Mountain"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Choosing a Mountain from the search puts it into hand")
    void choosingMountainPutsItIntoHand() {
        harness.setHand(player1, List.of(new ValleyRannet()));
        harness.addMana(player1, ManaColor.RED, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Forestcycling discards the card and offers only Forest cards")
    void forestcyclingDiscardsAndOffersForests() {
        harness.setHand(player1, List.of(new ValleyRannet()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibrary();

        harness.ensurePriority(player1);
        harness.getGameService().activateHandAbility(gd, player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Valley Rannet");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Forest"))
                .hasSize(2);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Mountain(),
                new Swamp(), new GrizzlyBears()));
    }

    @Test
    @DisplayName("Forestcycling puts the chosen Forest into hand without drawing another card")
    void choosingForestPutsItIntoHand() {
        harness.setHand(player1, List.of(new ValleyRannet()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.ensurePriority(player1);
        gs.activateHandAbility(gd, player1, 0, 1, null);

        harness.assertInGraveyard(player1, "Valley Rannet");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mountaincycling may fail to find even when a Mountain is available")
    void mountaincyclingMayFailToFind() {
        harness.setHand(player1, List.of(new ValleyRannet()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Valley Rannet");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Forestcycling with no Forest in the library does not draw a card")
    void forestcyclingWithoutMatchingCard() {
        harness.setHand(player1, List.of(new ValleyRannet()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLibrary(player1, List.of(new Mountain(), new Swamp(), new GrizzlyBears()));

        harness.ensurePriority(player1);
        gs.activateHandAbility(gd, player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Valley Rannet");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Neither cycling ability can be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new ValleyRannet()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> gs.activateHandAbility(gd, player1, 0, index, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana");
        }

        harness.assertInHand(player1, "Valley Rannet");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
