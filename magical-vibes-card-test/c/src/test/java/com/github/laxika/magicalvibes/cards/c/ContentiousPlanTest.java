package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Snarespinner;
import com.github.laxika.magicalvibes.cards.g.GodEternalKefnet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContentiousPlan.class, Snarespinner.class, GodEternalKefnet.class})
class ContentiousPlanTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as a sorcery spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ContentiousPlan()));
        addMana();

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Resolving proliferates and draws a card")
    void proliferatesAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new ContentiousPlan()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Proliferate can choose none and still draws a card")
    void proliferateChooseNoneStillDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new Snarespinner());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new ContentiousPlan()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws a card without a proliferate choice when no counters exist")
    void drawsWhenNoEligiblePermanents() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new ContentiousPlan()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Contentious Plan");
    }

    @Test
    @DisplayName("Proliferate adds every existing counter kind to chosen permanents and players only")
    void proliferatesAllKindsOnChosenPermanentsAndPlayers() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new Snarespinner());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        chosen.setCounterCount(CounterType.CHARGE, 3);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        gd.setPlayerEnergyCounters(player2.getId(), 4);
        gd.playerRadCounters.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new ContentiousPlan()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId(), player2.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(chosen.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(chosen.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A creature made lethal by proliferate still sees the subsequent card draw")
    void lethalProliferationDoesNotRemoveDrawAbilityBeforeDrawing() {
        Permanent kefnet = harness.addToBattlefieldAndReturn(player1, new GodEternalKefnet());
        kefnet.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        gd.cardsDrawnThisTurn.put(player1.getId(), 0);
        harness.setLibrary(player1, List.of(new ContentiousPlan(), new Snarespinner(), new Snarespinner()));
        harness.setHand(player1, List.of(new ContentiousPlan()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(kefnet.getId()));

        harness.assertInHand(player1, "Contentious Plan");
        harness.assertInGraveyard(player1, "God-Eternal Kefnet");
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
