package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.s.SproutSwarm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitterOrdeal.class, BladeOfTheSixthPride.class, Ghostfire.class, SproutSwarm.class})
class BitterOrdealTest extends BaseCardTest {

    @Test
    void exilesCardFromTargetPlayersLibrary() {
        Card exiledCard = new BladeOfTheSixthPride();
        Card remainingCard = new Ghostfire();
        harness.setLibrary(player2, List.of(exiledCard, remainingCard));

        castAndResolveBitterOrdeal(player2.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    void copiesForEachPermanentPutIntoGraveyardFromBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        destroyWithGhostfire(creature.getId());

        castBitterOrdeal();
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    void countsTokensPutIntoGraveyardFromBattlefield() {
        harness.setHand(player1, List.of(new SproutSwarm(), new SproutSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> saprolings = findPermanents(player1, "Saproling");
        destroyWithGhostfire(saprolings.get(0).getId());
        destroyWithGhostfire(saprolings.get(1).getId());

        castBitterOrdeal();
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    void gravestormCopyExilesAnotherCardWhenRetargetingIsDeclined() {
        Card firstCard = new BladeOfTheSixthPride();
        Card secondCard = new Ghostfire();
        Card thirdCard = new SproutSwarm();
        harness.setLibrary(player2, List.of(firstCard, secondCard, thirdCard));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        destroyWithGhostfire(creature.getId());

        castBitterOrdeal();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(thirdCard);
    }

    @Test
    void gravestormCopyMayBeRetargetedToAnotherPlayer() {
        Card player1FirstCard = new BladeOfTheSixthPride();
        Card player1SecondCard = new Ghostfire();
        Card player2FirstCard = new SproutSwarm();
        Card player2SecondCard = new BladeOfTheSixthPride();
        harness.setLibrary(player1, List.of(player1FirstCard, player1SecondCard));
        harness.setLibrary(player2, List.of(player2FirstCard, player2SecondCard));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        destroyWithGhostfire(creature.getId());

        castBitterOrdeal();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(player1FirstCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1SecondCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(player2FirstCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2SecondCard);
    }

    @Test
    void shufflesEmptyTargetPlayersLibraryWithoutPromptingForACard() {
        harness.setLibrary(player2, List.of());

        castAndResolveBitterOrdeal(player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castBitterOrdeal() {
        harness.setHand(player1, List.of(new BitterOrdeal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());
    }

    private void castAndResolveBitterOrdeal(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new BitterOrdeal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void destroyWithGhostfire(UUID targetId) {
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
