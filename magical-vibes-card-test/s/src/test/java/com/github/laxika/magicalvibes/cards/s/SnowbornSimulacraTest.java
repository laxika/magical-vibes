package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnowbornSimulacra.class, GrizzlyBears.class, PropheticPrism.class, ThayanEvokers.class,
        ShivanDragon.class})
class SnowbornSimulacraTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures a duplicate of each target into hand, castable with mana of any color")
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
    @DisplayName("With X of 5 or more, one of the duplicates may be put onto the battlefield")
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

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(5);
        assertThat(choice.maxCount()).isEqualTo(1);
        UUID chosenCardId = choice.cards().getFirst().getId();

        chooseOfferedCards(player1, 0);

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
    @DisplayName("Targets only nontoken permanents, an opponent's included")
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
    @DisplayName("With X of 0 nothing is conjured or offered")
    void zeroTargetsResolvesWithoutConjuringOrOfferingAChoice() {
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Snowborn Simulacra");
    }

    @Test
    @DisplayName("The put may be declined and offers only the duplicates")
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

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(5).doesNotContain(preexisting);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(preexisting.getId())))
                .isInstanceOf(IllegalStateException.class);
        chooseOfferedCards(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6).contains(preexisting);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Snowborn Simulacra");
    }

    @Test
    @DisplayName("With one legal target left, its duplicate is still offered, and the put still may be declined")
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
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactlyElementsOf(gd.playerHands.get(player1.getId()));
        chooseOfferedCards(player1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With no legal target left nothing is conjured or offered")
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
    @DisplayName("A duplicate put onto the battlefield gets its enter ability")
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
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        chooseOfferedCards(player1, 0);

        harness.assertOnBattlefield(player1, "Prophetic Prism");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5).contains(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The chosen duplicate enters from the hand")
    void chosenDuplicateEntersFromHand() {
        List<Permanent> targets = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new PropheticPrism()))
                .toList();
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 5, targets.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        UUID chosenId = choice.cards().getFirst().getId();
        chooseOfferedCards(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(chosenId))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getEnteredFromZone()).isEqualTo(Zone.HAND));
    }

    @Test
    @DisplayName("Conjuring several duplicates is one conjure event")
    void conjuringSeveralDuplicatesTriggersConjureAbilitiesOnce() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent evokers = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(evokers.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("A duplicate of a face-down target has only its face-down values, and the log names no card")
    void duplicateOfAFaceDownTargetIsANamelessTwoTwo() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new SnowbornSimulacra()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1, List.of(faceDown.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().satisfies(duplicate -> {
            assertThat(duplicate.getName()).isEmpty();
            assertThat(duplicate.getPower()).isEqualTo(2);
            assertThat(duplicate.getToughness()).isEqualTo(2);
        });
        assertThat(gd.gameLog).noneMatch(line -> line.plainText().contains("Shivan Dragon"));
    }
}
