package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaunaShaman.class, RuneclawBear.class, LlanowarElves.class, Mountain.class})
class FaunaShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability starts discard-cost choice for creature cards")
    void activationStartsDiscardChoice() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
        // Only creature cards should be valid (index 0 = RuneclawBear)
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Choosing a creature pays cost and puts ability on stack")
    void choosingCreaturePaysCostAndStacksAbility() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear(), new LlanowarElves()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotInHand(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Fauna Shaman");
    }

    @Test
    @DisplayName("Cannot activate without a creature card in hand")
    void cannotActivateWithoutCreatureCard() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a creature card");
    }

    @Test
    @DisplayName("Cannot choose non-creature for discard cost")
    void cannotChooseNonCreatureForDiscardCost() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);

        // Choosing an invalid index re-prompts instead of throwing
        harness.handleCardChosen(player1, 1);

        // State should still be awaiting discard cost choice (re-prompted)
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving ability presents library search for creatures")
    void resolvingPresentsLibrarySearch() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));
        setupLibraryWithCreatures();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.CREATURE));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature from library puts it into hand")
    void choosingCreatureFromLibraryPutsItIntoHand() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));
        setupLibraryWithCreatures();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Non-creature cards in library are excluded from search")
    void nonCreaturesExcludedFromSearch() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));

        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no creature cards"));
    }

    @Test
    @DisplayName("Player can fail to find with Fauna Shaman")
    void canFailToFind() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));
        setupLibraryWithCreatures();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate when summoning sick")
    void cannotActivateWhenSummoningSick() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new FaunaShaman());
        perm.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation pays green mana and taps the source before resolution")
    void paysManaAndTapCosts() {
        Permanent shaman = addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate an already tapped Fauna Shaman")
    void cannotActivateWhenTapped() {
        addReadyFaunaShaman(player1).tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Runeclaw Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the green activation cost")
    void cannotActivateWithoutGreenMana() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Runeclaw Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent activation or refund the discard")
    void resolvesWithEmptyLibrary() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("The ability still searches its controller's library after the source leaves")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent shaman = addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Mountain()));
        harness.setLibrary(player2, List.of(new RuneclawBear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(shaman);
        gd.playerGraveyards.get(player1.getId()).add(shaman.getCard());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A successful search reveals exactly the creature put into hand")
    void revealsChosenCreatureAndShuffles() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new RuneclawBear()));
        LlanowarElves found = new LlanowarElves();
        Mountain remaining = new Mountain();
        harness.setLibrary(player1, List.of(remaining, found));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(found);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(text -> text.contains("reveals") && text.contains("Llanowar Elves")
                        && text.contains("shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyFaunaShaman(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new FaunaShaman());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setupLibraryWithCreatures() {
        harness.setLibrary(player1, List.of(new LlanowarElves(), new RuneclawBear(), new Mountain()));
    }
}
