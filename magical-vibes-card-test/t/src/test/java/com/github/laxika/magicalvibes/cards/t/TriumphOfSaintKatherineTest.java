package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriumphOfSaintKatherine.class, Plains.class})
class TriumphOfSaintKatherineTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it returns with the top six cards in a shuffled seven-card pile")
    void deathTriggerExilesItAndShufflesTopSixCards() {
        List<Card> topCards = List.of(
                new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains());
        Card cardBelowPile = new Plains();
        harness.setLibrary(player1, List.of(
                topCards.get(0), topCards.get(1), topCards.get(2),
                topCards.get(3), topCards.get(4), topCards.get(5), cardBelowPile));
        Permanent triumph = harness.addToBattlefieldAndReturn(player1, new TriumphOfSaintKatherine());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, triumph));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(triumph.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(triumph.getCard());
        List<Card> expectedPile = new ArrayList<>(topCards);
        expectedPile.add(triumph.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 7))
                .containsExactlyInAnyOrderElementsOf(expectedPile);
        assertThat(gd.playerDecks.get(player1.getId()).get(7)).isSameAs(cardBelowPile);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5})
    @DisplayName("Fewer than six library cards prevents paying the exile cost")
    void shortLibraryLeavesTriumphInGraveyard(int librarySize) {
        List<Card> library = IntStream.range(0, librarySize)
                .mapToObj(i -> (Card) new Plains()).toList();
        harness.setLibrary(player1, library);
        Permanent triumph = harness.addToBattlefieldAndReturn(player1, new TriumphOfSaintKatherine());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, triumph));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(triumph.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(triumph.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("A stolen Triumph does not trigger when it enters its owner's graveyard")
    void stolenTriumphDoesNotTrigger() {
        Permanent triumph = harness.addToBattlefieldAndReturn(player2, new TriumphOfSaintKatherine());
        gd.stolenCreatures.put(triumph.getId(), player1.getId());
        List<Card> library = List.of(new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains());
        harness.setLibrary(player2, library);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, triumph));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(triumph.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("The ability does nothing if Triumph leaves the graveyard before resolution")
    void missingSourceLeavesLibraryUnchanged() {
        List<Card> library = List.of(new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains());
        harness.setLibrary(player1, library);
        Permanent triumph = harness.addToBattlefieldAndReturn(player1, new TriumphOfSaintKatherine());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, triumph));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardByIdForExile(gd, triumph.getCard().getId()));
        harness.setExile(player1, List.of(triumph.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(triumph.getCard());
    }

    @Test
    @DisplayName("Leaving and reentering the graveyard makes Triumph a new object")
    void oldTriggerCannotExileNewGraveyardObject() {
        List<Card> library = List.of(new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains());
        harness.setLibrary(player1, library);
        Permanent triumph = harness.addToBattlefieldAndReturn(player1, new TriumphOfSaintKatherine());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, triumph));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardByIdForExile(gd, triumph.getCard().getId()));
        harness.setHand(player1, List.of(triumph.getCard()));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(triumph.getCard()));
        gd.markGraveyardEntry(triumph.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(triumph.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(triumph.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Miracle casts the first drawn Triumph for one generic and one white mana")
    void miracleCastsForTwoMana() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            TriumphOfSaintKatherine triumph = new TriumphOfSaintKatherine();
            harness.setLibrary(player1, List.of(triumph, new Plains()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Triumph of Saint Katherine");
            assertThat(gd.playerHands.get(player1.getId())).doesNotContain(triumph);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Declining to reveal Triumph leaves it in hand")
    void declineMiracleReveal() {
        TriumphOfSaintKatherine triumph = new TriumphOfSaintKatherine();
        harness.setLibrary(player1, List.of(triumph, new Plains()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(triumph);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second card drawn this turn cannot use miracle")
    void secondDrawDoesNotOfferMiracle() {
        TriumphOfSaintKatherine triumph = new TriumphOfSaintKatherine();
        harness.setLibrary(player1, List.of(new Plains(), triumph, new Plains()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(triumph);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the miracle cast leaves Triumph in hand and spends no mana")
    void declineMiracleCast() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            TriumphOfSaintKatherine triumph = new TriumphOfSaintKatherine();
            harness.setLibrary(player1, List.of(triumph, new Plains()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerHands.get(player1.getId())).contains(triumph);
            harness.assertNotOnBattlefield(player1, "Triumph of Saint Katherine");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Miracle permits casting Triumph during the opponent's turn")
    void miracleCastsDuringOpponentsTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            TriumphOfSaintKatherine triumph = new TriumphOfSaintKatherine();
            harness.setLibrary(player1, List.of(triumph, new Plains()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Triumph of Saint Katherine");
            assertThat(gd.playerHands.get(player1.getId())).doesNotContain(triumph);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Combat damage gains life through lifelink")
    void combatDamageGainsLife() {
        addCreatureReady(player1, new TriumphOfSaintKatherine());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }
}
