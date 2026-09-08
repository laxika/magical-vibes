package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SkyshipWeatherlightTest extends BaseCardTest {

    @Test
    @DisplayName("ETB searches for any number of artifact and creature cards")
    void etbSearchesForArtifactsAndCreatures() {
        harness.setLibrary(player1, List.of(new HowlingMine(), new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new SkyshipWeatherlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Howling Mine", "Grizzly Bears");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        UUID sourceId = harness.getPermanentId(player1, "Skyship Weatherlight");
        assertThat(gd.getCardsExiledByPermanent(sourceId))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Howling Mine", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Activated ability returns exactly one randomly chosen exiled card to its owner")
    void activatedAbilityReturnsOneRandomCardToItsOwner() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent skyship = harness.addToBattlefieldAndReturn(player1, new SkyshipWeatherlight());
        Card artifact = new HowlingMine();
        Card creature = new GrizzlyBears();
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
}
