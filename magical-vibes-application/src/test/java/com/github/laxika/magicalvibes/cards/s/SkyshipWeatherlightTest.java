package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DralnusPet;
import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyshipWeatherlight.class, ManaCylix.class, DralnusPet.class, ForsakenCity.class})
class SkyshipWeatherlightTest extends BaseCardTest {

    @Test
    @DisplayName("ETB searches for any number of artifact and creature cards")
    void etbSearchesForArtifactsAndCreatures() {
        harness.setLibrary(player1, List.of(new ManaCylix(), new DralnusPet(), new ForsakenCity()));
        harness.setHand(player1, List.of(new SkyshipWeatherlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mana Cylix", "Dralnu's Pet");

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        UUID sourceId = harness.getPermanentId(player1, "Skyship Weatherlight");
        assertThat(gd.getCardsExiledByPermanent(sourceId))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mana Cylix", "Dralnu's Pet");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forsaken City");
        assertThat(gameLogContains("library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("ETB may choose no cards and still shuffle the library")
    void etbMayChooseNoCards() {
        harness.setLibrary(player1, List.of(new ManaCylix(), new ForsakenCity()));
        harness.setHand(player1, List.of(new SkyshipWeatherlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Mana Cylix");

        harness.handleCardChosen(player1, -1);

        UUID sourceId = harness.getPermanentId(player1, "Skyship Weatherlight");
        assertThat(gd.getCardsExiledByPermanent(sourceId)).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mana Cylix", "Forsaken City");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Activated ability returns exactly one randomly chosen exiled card to its owner")
    void activatedAbilityReturnsOneRandomCardToItsOwner() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent skyship = harness.addToBattlefieldAndReturn(player1, new SkyshipWeatherlight());
        Card artifact = new ManaCylix();
        Card creature = new DralnusPet();
        gd.addToExile(player2.getId(), artifact, skyship.getId());
        gd.addToExile(player1.getId(), creature, skyship.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Set<UUID> remainingIds = gd.exiledCards.stream()
                .filter(exiled -> skyship.getId().equals(exiled.sourcePermanentId()))
                .map(exiled -> exiled.card().getId())
                .collect(java.util.stream.Collectors.toSet());
        assertThat(remainingIds).hasSize(1);
        UUID returnedId = Set.of(artifact.getId(), creature.getId()).stream()
                .filter(id -> !remainingIds.contains(id))
                .findFirst()
                .orElseThrow();
        UUID ownerId = returnedId.equals(artifact.getId()) ? player2.getId() : player1.getId();
        assertThat(gd.playerHands.get(ownerId)).extracting(Card::getId).contains(returnedId);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void etbMayStopAfterOneCardAndActivationUsesThatSearch() {
        Card artifact = new ManaCylix();
        Card creature = new DralnusPet();
        harness.setLibrary(player1, List.of(artifact, creature, new ForsakenCity()));
        harness.setHand(player1, List.of(new SkyshipWeatherlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        UUID sourceId = harness.getPermanentId(player1, "Skyship Weatherlight");
        assertThat(gd.getCardsExiledByPermanent(sourceId)).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).contains(creature).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("library is shuffled")).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mana Cylix");
        assertThat(gd.getCardsExiledByPermanent(sourceId)).isEmpty();
    }

    @Test
    void etbCanExileMultipleCardsWithTheSameName() {
        Card first = new ManaCylix();
        Card second = new ManaCylix();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SkyshipWeatherlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        UUID sourceId = harness.getPermanentId(player1, "Skyship Weatherlight");
        assertThat(gd.getCardsExiledByPermanent(sourceId)).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("library is shuffled")).isTrue();
    }

    @Test
    void etbWithNoMatchingCardsStillShuffles() {
        Card land = new ForsakenCity();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new SkyshipWeatherlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Skyship Weatherlight");
        assertThat(gd.getCardsExiledByPermanent(sourceId)).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    void activationWithNoLinkedCardsDoesNotReturnUnrelatedExiledCards() {
        harness.setHand(player1, List.of());
        Permanent skyship = harness.addToBattlefieldAndReturn(player1, new SkyshipWeatherlight());
        Card unrelated = new ManaCylix();
        gd.addToExile(player1.getId(), unrelated, UUID.randomUUID());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(skyship.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactly(unrelated.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
