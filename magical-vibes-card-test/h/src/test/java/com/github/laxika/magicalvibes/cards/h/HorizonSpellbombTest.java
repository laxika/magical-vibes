package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HorizonSpellbomb.class, Forest.class, Island.class, Plains.class, GrizzlyBears.class})
class HorizonSpellbombTest extends BaseCardTest {


    @Test
    @DisplayName("Activating ability sacrifices spellbomb and prompts death trigger")
    void activateAbilitySacrificesAndPromptsMayAbility() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        // Clear deck so the search ability finds nothing and auto-completes
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        // Spellbomb should be sacrificed
        harness.assertNotOnBattlefield(player1, "Horizon Spellbomb");
        harness.assertInGraveyard(player1, "Horizon Spellbomb");

        // Death trigger MayPayManaEffect resolves first (on top per CR 603.3)
        harness.passBothPriorities();

        // Death trigger may ability should prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Activated ability searches library for basic land and puts it in hand")
    void activatedAbilitySearchesForBasicLand() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibraryWithBasicLands();

        harness.activateAbility(player1, 0, null, null);

        // Death trigger MayPayManaEffect resolves first (on top per CR 603.3)
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline death trigger

        // Resolve the search ability
        harness.passBothPriorities();

        // Should be awaiting library search
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Choosing a basic land from search puts it into hand")
    void choosingBasicLandPutsItIntoHand() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibraryWithBasicLands();

        harness.activateAbility(player1, 0, null, null);

        // Death trigger MayPayManaEffect resolves first (on top per CR 603.3)
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline death trigger

        // Resolve the search ability
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }


    @Test
    @DisplayName("Accepting death trigger and paying {G} draws a card")
    void acceptDeathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Use non-basic-land library so search auto-completes (test focuses on death trigger)
        setupLibraryWithNonBasicLands();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Death trigger MayPayManaEffect resolves first (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept death trigger — pay {G}, inner draw resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Green mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);

        // Resolve the search ability (finds no basic lands, auto-completes)
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining death trigger does not draw a card")
    void declineDeathTriggerNoCard() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Clear deck so search auto-completes (test focuses on death trigger)
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        // Death trigger MayPayManaEffect resolves first (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // Green mana should not be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        // Resolve the search ability (finds nothing)
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting death trigger without enough mana treats as decline")
    void acceptWithoutManaNoCard() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        // No green mana added
        // Clear deck so search auto-completes (test focuses on death trigger)
        harness.setLibrary(player1, List.of());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Death trigger MayPayManaEffect resolves first (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept but cannot pay {G} — auto-treats as decline
        harness.handleMayAbilityChosen(player1, true);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);

        // Resolve the search ability (finds nothing)
        harness.passBothPriorities();
    }


    @Test
    @DisplayName("Both abilities work: search for land AND draw a card")
    void bothAbilitiesWork() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibraryWithBasicLands();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Death trigger MayPayManaEffect resolves first (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept death trigger — pay {G} to draw, inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Resolve search ability
        harness.passBothPriorities();

        // Should be awaiting library search
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);

        // Hand should have grown by 2 (draw + search)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }

    @Test
    @DisplayName("A basic land search may find nothing even when basic lands are available")
    void mayFailToFindBasicLand() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibraryWithBasicLands();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Shatter.class})
    @DisplayName("Destruction triggers the draw ability without activating the land search")
    void destructionTriggersDrawWithoutSearch() {
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Horizon Spellbomb"));
        harness.assertInGraveyard(player1, "Horizon Spellbomb");
        harness.assertNotOnBattlefield(player1, "Horizon Spellbomb");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void setupLibraryWithBasicLands() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }

    private void setupLibraryWithNonBasicLands() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
    }
}
