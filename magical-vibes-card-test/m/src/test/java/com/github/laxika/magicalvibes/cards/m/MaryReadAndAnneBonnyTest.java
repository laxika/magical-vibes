package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.Adrestia;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaryReadAndAnneBonny.class, Island.class, Adrestia.class, Plains.class})
class MaryReadAndAnneBonnyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and discards, creating a tapped Treasure for an Island")
    void islandDiscardCreatesTappedTreasure() {
        lootAndDiscard(new Island());

        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Creates a tapped Treasure for a Pirate discard")
    void pirateDiscardCreatesTappedTreasure() {
        lootAndDiscard(new MaryReadAndAnneBonny());

        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Creates a tapped Treasure for a Vehicle discard")
    void vehicleDiscardCreatesTappedTreasure() {
        lootAndDiscard(new Adrestia());

        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Does not create a Treasure for an unrelated discard")
    void unrelatedDiscardDoesNotCreateTreasure() {
        lootAndDiscard(new Plains());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Haste permits looting immediately and the tap cost prevents a second activation")
    void hasteAllowsImmediateLooting() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MaryReadAndAnneBonny());
        source.setSummoningSick(true);
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Plains()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Plains.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().isInstanceOf(Island.class);
        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Starting with an empty hand still draws and discards the drawn Island")
    void emptyHandDiscardsDrawnCard() {
        addCreatureReady(player1, new MaryReadAndAnneBonny());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Plains()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().isInstanceOf(Island.class);
        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("An opponent's Island discard creates Treasure only for that opponent")
    void opponentDiscardDoesNotTriggerController() {
        addCreatureReady(player1, new MaryReadAndAnneBonny());
        addCreatureReady(player2, new MaryReadAndAnneBonny());
        harness.setHand(player2, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Plains()));

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    private void lootAndDiscard(Card discardedCard) {
        addCreatureReady(player1, new MaryReadAndAnneBonny());
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(new Plains()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
