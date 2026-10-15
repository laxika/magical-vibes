package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.ChromeCat;
import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllicitShipment.class, Forest.class, ChromeCat.class, CivicGardener.class})
class IllicitShipmentTest extends BaseCardTest {

    @Test
    void searchesLibraryForAnyCardAndPutsItIntoHand() {
        Card shipment = new IllicitShipment();
        Card chosenCard = new CivicGardener();
        harness.setHand(player1, List.of(shipment));
        harness.setLibrary(player1, List.of(new Forest(), chosenCard));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().reveals()).isFalse();

        harness.handleCardChosen(player1, search.params().cards().indexOf(chosenCard));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(chosenCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(shipment.getId()));
    }

    @Test
    void casualtyCopiesTheSpellAndSacrificesTheChosenCreature() {
        Permanent casualtyCreature = addCreatureReady(player1, new ChromeCat());
        Card shipment = new IllicitShipment();
        Card firstChoice = new Forest();
        Card secondChoice = new CivicGardener();
        harness.setHand(player1, List.of(shipment));
        harness.setLibrary(player1, List.of(firstChoice, secondChoice));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId());
        resolveAllTriggers();
        chooseFirstSearchCard(firstChoice);
        harness.passBothPriorities();
        chooseFirstSearchCard(secondChoice);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstChoice.getId(), secondChoice.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(shipment.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    void cannotPayCasualtyWithAnUnderpoweredCreature() {
        Permanent casualtyCreature = addCreatureReady(player1, new CivicGardener());
        harness.setHand(player1, List.of(new IllicitShipment()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3");
    }

    @Test
    void casualtyIsOptionalEvenWithAnEligibleCreature() {
        Permanent creature = addCreatureReady(player1, new ChromeCat());
        Card chosenCard = new Forest();
        Card remainingCard = new CivicGardener();
        harness.setHand(player1, List.of(new IllicitShipment()));
        harness.setLibrary(player1, List.of(chosenCard, remainingCard));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        chooseFirstSearchCard(chosenCard);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void resolvesNormallyWithAnEmptyLibrary() {
        Card shipment = new IllicitShipment();
        harness.setHand(player1, List.of(shipment));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shipment);
    }

    @Test
    void originalResolvesWithAnEmptyLibraryAfterTheCopyFindsTheOnlyCard() {
        Card creatureCard = new ChromeCat();
        Permanent creature = addCreatureReady(player1, creatureCard);
        Card shipment = new IllicitShipment();
        Card chosenCard = new Forest();
        harness.setHand(player1, List.of(shipment));
        harness.setLibrary(player1, List.of(chosenCard));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creatureCard);

        resolveAllTriggers();
        chooseFirstSearchCard(chosenCard);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(creatureCard, shipment);
    }

    @Test
    void cannotDeclineToFindACardInANonemptyLibrary() {
        Card chosenCard = new Forest();
        harness.setHand(player1, List.of(new IllicitShipment()));
        harness.setLibrary(player1, List.of(chosenCard));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);

        chooseFirstSearchCard(chosenCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeAnOpponentsCreatureForCasualty() {
        Permanent creature = addCreatureReady(player2, new ChromeCat());
        Card shipment = new IllicitShipment();
        harness.setHand(player1, List.of(shipment));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shipment);
        assertThat(gd.stack).isEmpty();
    }

    private void chooseFirstSearchCard(Card card) {
        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.handleCardChosen(player1, search.params().cards().indexOf(card));
    }
}
