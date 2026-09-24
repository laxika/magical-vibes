package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlanarPortal.class, Swamp.class, GrizzlyBears.class})
class PlanarPortalTest extends BaseCardTest {

    @Test
    @DisplayName("Activating taps the portal, spends the mana, and puts the ability on the stack")
    void activatingTapsAndPutsOnStack() {
        Permanent portal = addReadyPortal(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThat(portal.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Resolving presents all cards from library for an unrestricted search")
    void resolvingPresentsLibraryForSearch() {
        addReadyPortal(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and shuffles the library")
    void choosingCardPutsItIntoHand() {
        addReadyPortal(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player1, List.of());
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains(player1.getUsername() + "'s library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("An empty library search resolves without offering a choice")
    void emptyLibrarySearchResolvesWithoutChoice() {
        addReadyPortal(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyPortal(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent portal = addReadyPortal(player1);
        portal.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("An empty library search resolves without a card choice")
    void emptyLibrarySearchResolvesWithoutChoice() {
        addReadyPortal(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyPortal(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PlanarPortal());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
