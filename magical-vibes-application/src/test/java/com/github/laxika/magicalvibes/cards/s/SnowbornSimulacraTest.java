package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnowbornSimulacra.class, GrizzlyBears.class, PropheticPrism.class})
class SnowbornSimulacraTest extends BaseCardTest {

    @Test
    void conjuresDuplicatesWithPerpetualAnyColorCastingPermission() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        List<Card> duplicates = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .toList();
        assertThat(duplicates).hasSize(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int duplicateIndex = gd.playerHands.get(player1.getId()).indexOf(duplicates.getFirst());
        harness.castCreature(player1, duplicateIndex);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    void atFiveOrMoreOffersOneNewDuplicateForBattlefieldEntry() {
        List<Permanent> targets = List.of(
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 5, targets.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).hasSize(5);
        UUID chosenCardId = gd.playerHands.get(player1.getId())
                .get(choice.validIndices().getFirst()).getId();

        harness.handleCardChosen(player1, choice.validIndices().getFirst());

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .hasSize(4)
                .containsOnly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(chosenCardId);
    }

    @Test
    void targetsMustBeNontokenPermanentsAndMayBeControlledByAnOpponent() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(token.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken permanents");
        harness.castSorcery(player1, 0, 1, List.of(opponentPermanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void zeroTargetsResolvesWithoutConjuringOrOfferingAChoice() {
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Snowborn Simulacra");
    }

    @Test
    void atFiveMayDeclineAndCannotChooseAPreexistingHandCard() {
        List<Permanent> targets = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new PropheticPrism()))
                .toList();
        Card preexisting = new PropheticPrism();
        harness.setHand(player1, List.of(new SnowbornSimulacra(), preexisting));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 5, targets.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1, 2, 3, 4, 5);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6).contains(preexisting);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Snowborn Simulacra");
    }

    @Test
    void fiveStillOffersBattlefieldEntryWhenOnlyOneTargetRemainsLegal() {
        List<Permanent> targets = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new PropheticPrism()))
                .toList();
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 5, targets.stream().map(Permanent::getId).toList());
        gd.playerBattlefields.get(player2.getId()).removeAll(targets.subList(0, 4));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void allTargetsLeavingPreventsConjuringAndBattlefieldChoice() {
        List<Permanent> targets = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new PropheticPrism()))
                .toList();
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 5, targets.stream().map(Permanent::getId).toList());
        gd.playerBattlefields.get(player2.getId()).removeAll(targets);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Snowborn Simulacra");
    }

    @Test
    void puttingConjuredArtifactOntoBattlefieldTriggersItsEnterAbility() {
        List<Permanent> targets = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new PropheticPrism()))
                .toList();
        Card libraryCard = new SnowbornSimulacra();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 5, targets.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        harness.handleCardChosen(player1, choice.validIndices().getFirst());

        harness.assertOnBattlefield(player1, "Prophetic Prism");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5).contains(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void chosenDuplicateEntersFromHand() {
        List<Permanent> targets = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new PropheticPrism()))
                .toList();
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 5, targets.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        UUID chosenId = gd.playerHands.get(player1.getId()).get(choice.validIndices().getFirst()).getId();
        harness.handleCardChosen(player1, choice.validIndices().getFirst());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(chosenId))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getEnteredFromZone()).isEqualTo(Zone.HAND));
    }
}
