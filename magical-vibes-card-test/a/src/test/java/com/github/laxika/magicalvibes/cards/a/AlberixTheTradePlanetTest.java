package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlberixTheTradePlanet.class, Forest.class, GrizzlyBears.class, Island.class})
class AlberixTheTradePlanetTest extends BaseCardTest {

    private static final String DISCARD_MODE =
            "Discard a card. If you do, put two of Alberix's resources into its owner's hand.";
    private static final String EXILE_MODE = "Exile the top card of your library as a resource.";

    @Test
    @DisplayName("Enters by exiling five face-up resources")
    void entersWithFiveResources() {
        List<Card> library = List.of(
                new Forest(), new Island(), new Forest(), new Island(), new Forest(), new Island());
        UUID alberixId = castAndResolve(library);

        assertThat(gd.getCardsExiledByPermanent(alberixId)).hasSize(5);
        assertThat(gd.exiledCards).filteredOn(e -> alberixId.equals(e.sourcePermanentId()))
                .allMatch(e -> !e.faceDown());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(5));
    }

    @Test
    @DisplayName("Trade Routes can exile another resource")
    void exilesAnotherResource() {
        List<Card> library = new ArrayList<>(List.of(
                new Forest(), new Island(), new Forest(), new Island(), new Forest(), new Island()));
        UUID alberixId = castAndResolve(library);

        advanceToPrecombatMain();
        harness.passBothPriorities();
        harness.handleListChoice(player1, EXILE_MODE);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(alberixId)).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding a card returns two resources to their owner's hand")
    void discardingReturnsTwoResources() {
        List<Card> library = new ArrayList<>(List.of(
                new Forest(), new Island(), new Forest(), new Island(), new Forest(), new Island()));
        UUID alberixId = castAndResolve(library);
        Card discard = new GrizzlyBears();
        harness.setHand(player1, List.of(discard));

        advanceToPrecombatMain();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibraryRevealChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(firstChoice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(firstChoice.validCardIds().getFirst()));

        PendingInteraction.LibraryRevealChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(secondChoice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(secondChoice.validCardIds().getFirst()));

        assertThat(gd.getCardsExiledByPermanent(alberixId)).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactly(discard.getId());
    }

    @Test
    @DisplayName("Trade Routes chooses its mode before opponents can respond")
    void choosesModeWhenTriggerIsPutOnStack() {
        castAndResolve(List.of(new Forest(), new Island(), new Forest(), new Island(), new Forest()));
        advanceToPrecombatMain();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, EXILE_MODE);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resource returns are part of the resolving ability, without another priority round")
    void returnsResourcesDuringSameResolution() {
        UUID alberixId = castAndResolve(List.of(new Forest()));
        Card discard = new GrizzlyBears();
        harness.setHand(player1, List.of(discard));

        advanceToPrecombatMain();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(alberixId)).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A new controller can trade resources exiled by the previous controller")
    void tradesResourcesAfterControlChanges() {
        UUID alberixId = castAndResolve(List.of(new Forest()));
        var alberix = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(alberix);
        gd.playerBattlefields.get(player2.getId()).add(alberix);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
        harness.handleListChoice(player2, DISCARD_MODE);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(alberixId)).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Planet exiles all available cards when fewer than five remain")
    void exilesShortLibrary() {
        UUID alberixId = castAndResolve(List.of(new Forest(), new Island()));
        assertThat(gd.getCardsExiledByPermanent(alberixId)).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing discard with an empty hand does not return resources")
    void emptyHandDoesNotReturnResources() {
        UUID alberixId = castAndResolve(List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of());
        advanceToPrecombatMain();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(alberixId)).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private UUID castAndResolve(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new AlberixTheTradePlanet()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return harness.getPermanentId(player1, "Alberix, the Trade Planet");
    }

    private void advanceToPrecombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }
}
