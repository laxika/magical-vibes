package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuggedHighlands;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreaterTanuki.class, Forest.class, RuggedHighlands.class})
class GreaterTanukiTest extends BaseCardTest {

    @Test
    @DisplayName("Channel discards Greater Tanuki and puts a basic land onto the battlefield tapped")
    void channelSearchesForBasicLand() {
        harness.setHand(player1, List.of(new GreaterTanuki()));
        harness.setLibrary(player1, List.of(new Forest(), new GreaterTanuki(), new RuggedHighlands()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertNotInHand(player1, "Greater Tanuki");
        harness.assertInGraveyard(player1, "Greater Tanuki");
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Greater Tanuki");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(1);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(2)
                .noneMatch(card -> card instanceof Forest);
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Channel may fail to find even when a basic land is available")
    void channelMayFailToFind() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new GreaterTanuki()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Greater Tanuki");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel resolves without finding a land when the library has no basic lands")
    void channelWithNoBasicLands() {
        GreaterTanuki tanuki = new GreaterTanuki();
        RuggedHighlands land = new RuggedHighlands();
        harness.setHand(player1, List.of(new GreaterTanuki()));
        harness.setLibrary(player1, List.of(tanuki, land));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Greater Tanuki");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(tanuki, land);
        harness.assertNotOnBattlefield(player1, "Rugged Highlands");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel resolves with an empty library")
    void channelWithEmptyLibrary() {
        harness.setHand(player1, List.of(new GreaterTanuki()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Greater Tanuki");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel cannot be activated without green mana")
    void channelRequiresGreenMana() {
        harness.setHand(player1, List.of(new GreaterTanuki()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Greater Tanuki");
        harness.assertNotInGraveyard(player1, "Greater Tanuki");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel requires three mana in total")
    void channelRequiresFullManaCost() {
        harness.setHand(player1, List.of(new GreaterTanuki()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Greater Tanuki");
        harness.assertNotInGraveyard(player1, "Greater Tanuki");
        assertThat(gd.stack).isEmpty();
    }
}
