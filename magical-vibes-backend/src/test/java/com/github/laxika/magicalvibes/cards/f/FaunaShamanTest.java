package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FaunaShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Fauna Shaman has correct activated ability")
    void hasCorrectProperties() {
        FaunaShaman card = new FaunaShaman();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{G}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(0)).isInstanceOf(DiscardCardTypeCost.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(1)).isInstanceOf(SearchLibraryForCardTypesToHandEffect.class);
        SearchLibraryForCardTypesToHandEffect searchEffect =
                (SearchLibraryForCardTypesToHandEffect) card.getActivatedAbilities().getFirst().getEffects().get(1);
        assertThat(searchEffect.cardTypes()).isEqualTo(Set.of(CardType.CREATURE));
    }

    @Test
    @DisplayName("Activating ability starts discard-cost choice for creature cards")
    void activationStartsDiscardChoice() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE);
        assertThat(gd.stack).isEmpty();
        // Only creature cards should be valid (index 0 = GrizzlyBears)
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Choosing a creature pays cost and puts ability on stack")
    void choosingCreaturePaysCostAndStacksAbility() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new LlanowarElves()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
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
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Resolving ability presents library search for creatures")
    void resolvingPresentsLibrarySearch() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setupLibraryWithCreatures();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.getType() == CardType.CREATURE
                        || c.getAdditionalTypes().contains(CardType.CREATURE));
        assertThat(gd.interaction.librarySearch().reveals()).isTrue();
        assertThat(gd.interaction.librarySearch().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature from library puts it into hand")
    void choosingCreatureFromLibraryPutsItIntoHand() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setupLibraryWithCreatures();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Non-creature cards in library are excluded from search")
    void nonCreaturesExcludedFromSearch() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Mountain(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no creature cards"));
    }

    @Test
    @DisplayName("Player can fail to find with Fauna Shaman")
    void canFailToFind() {
        addReadyFaunaShaman(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setupLibraryWithCreatures();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Cannot activate when summoning sick")
    void cannotActivateWhenSummoningSick() {
        FaunaShaman card = new FaunaShaman();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(perm);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyFaunaShaman(Player player) {
        FaunaShaman card = new FaunaShaman();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupLibraryWithCreatures() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new LlanowarElves(), new GrizzlyBears(), new Mountain()));
    }
}
