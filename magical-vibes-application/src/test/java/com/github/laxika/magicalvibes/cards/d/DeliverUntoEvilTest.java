package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JayasGreeting;
import com.github.laxika.magicalvibes.cards.l.LazotepReaver;
import com.github.laxika.magicalvibes.cards.n.NicolBolasDragonGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeliverUntoEvil.class, LazotepReaver.class, Island.class, NicolBolasDragonGod.class, JayasGreeting.class})
class DeliverUntoEvilTest extends BaseCardTest {

    @Test
    void opponentLeavesTwoTargetsInGraveyardAndReturnsTheRest() {
        Card island = new Island();
        Card greeting = new JayasGreeting();
        Card reaver = new LazotepReaver();
        Card fourth = new Island();
        List<Card> targets = List.of(island, greeting, reaver, fourth);
        harness.setGraveyard(player1, targets);
        cast(targets);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player2, List.of(greeting.getId(), fourth.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(greeting, fourth);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, reaver);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void bolasPlaneswalkerReturnsAllTargetsWithoutAnOpponentChoice() {
        Card island = new Island();
        Card greeting = new JayasGreeting();
        List<Card> targets = List.of(island, greeting);
        harness.setGraveyard(player1, targets);
        var bolas = harness.addToBattlefieldAndReturn(player1, new NicolBolasDragonGod());
        bolas.setCounterCount(CounterType.LOYALTY, 4);
        cast(targets);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, greeting);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void canChooseNoTargetsAndStillExilesItself() {
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new DeliverUntoEvil(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void canChooseNoTargetsFromANonemptyGraveyard() {
        Card island = new Island();
        harness.setGraveyard(player1, List.of(island));
        cast(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void oneTargetStaysInGraveyardWithoutBolas() {
        Card island = new Island();
        harness.setGraveyard(player1, List.of(island));
        cast(List.of(island));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player2, List.of(island.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void opponentsBolasDoesNotReturnTargets() {
        Card island = new Island();
        Card greeting = new JayasGreeting();
        List<Card> targets = List.of(island, greeting);
        harness.setGraveyard(player1, targets);
        var bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasDragonGod());
        bolas.setCounterCount(CounterType.LOYALTY, 4);
        cast(targets);
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), greeting.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(targets);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void bolasEnteringAfterCastingReturnsTargets() {
        Card island = new Island();
        harness.setGraveyard(player1, List.of(island));
        castToStack(List.of(island));
        var bolas = harness.addToBattlefieldAndReturn(player1, new NicolBolasDragonGod());
        bolas.setCounterCount(CounterType.LOYALTY, 4);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void bolasLeavingBeforeResolutionRequiresOpponentChoice() {
        Card island = new Island();
        harness.setGraveyard(player1, List.of(island));
        var bolas = harness.addToBattlefieldAndReturn(player1, new NicolBolasDragonGod());
        bolas.setCounterCount(CounterType.LOYALTY, 4);
        castToStack(List.of(island));
        gd.playerBattlefields.get(player1.getId()).remove(bolas);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(island.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void opponentChoosesOnlyTargetsStillInGraveyard() {
        Card island = new Island();
        Card greeting = new JayasGreeting();
        Card reaver = new LazotepReaver();
        Card removed = new Island();
        List<Card> targets = List.of(island, greeting, reaver, removed);
        harness.setGraveyard(player1, targets);
        castToStack(targets);
        harness.setGraveyard(player1, List.of(island, greeting, reaver));
        harness.setExile(player1, List.of(removed));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), greeting.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(island, greeting);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(reaver);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(removed)
                .anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void allTargetsLeavingGraveyardPreventsResolutionAndExile() {
        Card island = new Island();
        harness.setGraveyard(player1, List.of(island));
        castToStack(List.of(island));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(island));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement()
                .isInstanceOf(DeliverUntoEvil.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(island);
    }

    @Test
    void bolasReturnsOnlyLegalTargetsAndLeavesUntargetedCards() {
        Card island = new Island();
        Card greeting = new JayasGreeting();
        Card untargeted = new LazotepReaver();
        harness.setGraveyard(player1, List.of(island, greeting, untargeted));
        var bolas = harness.addToBattlefieldAndReturn(player1, new NicolBolasDragonGod());
        bolas.setCounterCount(CounterType.LOYALTY, 4);
        castToStack(List.of(island, greeting));
        harness.setGraveyard(player1, List.of(island, untargeted));
        harness.setExile(player1, List.of(greeting));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(untargeted);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(greeting)
                .anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    private void cast(List<Card> targets) {
        castToStack(targets);
        harness.passBothPriorities();
    }

    private void castToStack(List<Card> targets) {
        harness.setHand(player1, List.of(new DeliverUntoEvil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MultiGraveyardChoice) {
            PendingInteraction.MultiGraveyardChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(choice.playerId()).isEqualTo(player1.getId());
            harness.handleMultipleCardsChosen(player1, targets.stream().map(Card::getId).toList());
        }
    }
}
