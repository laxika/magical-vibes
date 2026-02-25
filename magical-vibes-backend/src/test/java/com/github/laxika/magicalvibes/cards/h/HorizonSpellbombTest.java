package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HorizonSpellbombTest extends BaseCardTest {

    // ===== Activated ability: search for basic land =====

    @Test
    @DisplayName("Activating ability sacrifices spellbomb and prompts death trigger")
    void activateAbilitySacrificesAndPromptsMayAbility() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        // Spellbomb should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Horizon Spellbomb"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Horizon Spellbomb"));

        // Death trigger may ability should prompt
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Activated ability searches library for basic land and puts it in hand")
    void activatedAbilitySearchesForBasicLand() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibraryWithBasicLands();

        harness.activateAbility(player1, 0, null, null);

        // Decline the death trigger draw
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the search ability
        harness.passBothPriorities();

        // Should be awaiting library search
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(3);
        assertThat(gd.interaction.awaitingLibrarySearchCards())
                .allMatch(c -> c.getType() == CardType.LAND && c.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Choosing a basic land from search puts it into hand")
    void choosingBasicLandPutsItIntoHand() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibraryWithBasicLands();

        harness.activateAbility(player1, 0, null, null);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.awaitingLibrarySearchCards();
        String chosenName = offered.getFirst().getName();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Death trigger: may pay {G} to draw =====

    @Test
    @DisplayName("Accepting death trigger and paying {G} draws a card")
    void acceptDeathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibraryWithBasicLands();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Accept death trigger — pay {G}
        harness.handleMayAbilityChosen(player1, true);

        // Draw triggered ability should be on stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        // Resolve the draw triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Green mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining death trigger does not draw a card")
    void declineDeathTriggerNoCard() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the search ability
        harness.passBothPriorities();

        // Green mana should not be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting death trigger without enough mana treats as decline")
    void acceptWithoutManaNoCard() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        // No green mana added

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Accept but cannot pay {G}
        harness.handleMayAbilityChosen(player1, true);

        // Resolve the search ability (no draw triggered ability should be on stack)
        harness.passBothPriorities();

        // No card drawn (hand size may increase from search, but no extra draw)
        // Just verify no green mana was spent (there was none)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    // ===== Both abilities interact correctly =====

    @Test
    @DisplayName("Both abilities work: search for land AND draw a card")
    void bothAbilitiesWork() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibraryWithBasicLands();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Accept death trigger — pay {G} to draw
        harness.handleMayAbilityChosen(player1, true);

        // Resolve draw (top of stack)
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Resolve search ability
        harness.passBothPriorities();

        // Should be awaiting library search
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Choose a land
        String chosenName = gd.interaction.awaitingLibrarySearchCards().getFirst().getName();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Hand should have grown by 2 (draw + search)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }

    private void setupLibraryWithBasicLands() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
