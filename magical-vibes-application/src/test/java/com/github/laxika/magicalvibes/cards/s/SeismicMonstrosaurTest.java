package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicMonstrosaur.class, Mountain.class, Forest.class})
class SeismicMonstrosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land draws a card")
    void sacrificingLandDrawsACard() {
        harness.addToBattlefield(player1, new SeismicMonstrosaur());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mountaincycling searches for a Mountain and discards this card")
    void mountaincyclingSearchesForMountain() {
        harness.setHand(player1, List.of(new SeismicMonstrosaur()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Seismic Monstrosaur");
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId()))
                .allMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("The ability cannot be activated without a land to sacrifice")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SeismicMonstrosaur());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
    }

    @Test
    @DisplayName("A tapped Forest can be sacrificed and the cost is paid before drawing")
    void sacrificesTappedNonMountainBeforeResolution() {
        harness.addToBattlefield(player1, new SeismicMonstrosaur());
        harness.addToBattlefield(player1, new Forest());
        findPermanent(player1, "Forest").setTapped(true);
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Mountain");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Seismic Monstrosaur");
    }

    @Test
    @DisplayName("An opponent's land cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsLand() {
        harness.addToBattlefield(player1, new SeismicMonstrosaur());
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");

        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mountaincycling discards as a cost and may fail to find a Mountain")
    void mountaincyclingMayFailToFind() {
        harness.setHand(player1, List.of(new SeismicMonstrosaur()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Seismic Monstrosaur");
        harness.assertNotInHand(player1, "Seismic Monstrosaur");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mountaincycling resolves when no Mountains are in the library")
    void mountaincyclingWithoutMountains() {
        harness.setHand(player1, List.of(new SeismicMonstrosaur()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Seismic Monstrosaur");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Trample deals excess combat damage through a blocker")
    void tramplesOverBlocker() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new SeismicMonstrosaur());
        harness.addToBattlefield(player2, new SeismicMonstrosaur());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                findPermanent(player2, "Seismic Monstrosaur").getId(), 5,
                player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Seismic Monstrosaur");
        harness.assertInGraveyard(player2, "Seismic Monstrosaur");
    }
}
