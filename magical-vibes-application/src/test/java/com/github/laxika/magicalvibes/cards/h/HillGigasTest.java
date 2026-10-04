package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HillGigas.class, Forest.class, Mountain.class})
class HillGigasTest extends BaseCardTest {

    @Test
    @DisplayName("Mountaincycling discards the card and offers only Mountain cards")
    void mountaincyclingDiscardsAndOffersMountains() {
        harness.setHand(player1, List.of(new HillGigas()));
        harness.addMana(player1, ManaColor.RED, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Gigas");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Mountain"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Choosing a Mountain from mountaincycling puts it into hand")
    void choosingMountainPutsItIntoHand() {
        harness.setHand(player1, List.of(new HillGigas()));
        harness.addMana(player1, ManaColor.RED, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Mountaincycling pays the discard cost before the search resolves")
    void discardIsPaidBeforeResolution() {
        harness.setHand(player1, List.of(new HillGigas()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Hill Gigas");
        harness.assertNotInHand(player1, "Hill Gigas");
        harness.assertNotInHand(player1, "Mountain");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mountaincycling can fail to find even with a Mountain in the library")
    void canDeclineToFindMountain() {
        harness.setHand(player1, List.of(new HillGigas()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Hill Gigas");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mountaincycling resolves when there are no Mountains to find")
    void resolvesWithoutMatchingCard() {
        harness.setHand(player1, List.of(new HillGigas()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Gigas");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mountaincycling cannot discard the card with insufficient mana")
    void insufficientManaDoesNotDiscard() {
        harness.setHand(player1, List.of(new HillGigas()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Hill Gigas");
        harness.assertNotInGraveyard(player1, "Hill Gigas");
        assertThat(gd.stack).isEmpty();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Forest(), new HillGigas()));
    }
}
