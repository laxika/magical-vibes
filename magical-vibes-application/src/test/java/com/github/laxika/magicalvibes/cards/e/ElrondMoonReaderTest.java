package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElrondMoonReader.class, GrizzlyBears.class, Forest.class})
class ElrondMoonReaderTest extends BaseCardTest {

    @Test
    @DisplayName("Draws once when you activate a creature's ability and flickers up to two permanents")
    void drawsAndFlickersTargets() {
        Permanent elrond = addCreatureReady(player1, new ElrondMoonReader());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addElrondMana(1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstBear.getId(), secondBear.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(elrond);

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(3)
                .contains(elrond);
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void drawsOnlyOnceEachTurn() {
        addCreatureReady(player1, new ElrondMoonReader());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        seedLibrary(2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addElrondMana(2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(firstBear.getId()));
        resolveAllTriggers();
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(secondBear.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot target itself or a land")
    void rejectsInvalidTargets() {
        Permanent elrond = addCreatureReady(player1, new ElrondMoonReader());
        Permanent land = addCreatureReady(player1, new Forest());
        addElrondMana(1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(elrond.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating with no targets still draws a card")
    void drawsWithNoTargets() {
        Permanent elrond = addCreatureReady(player1, new ElrondMoonReader());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addElrondMana(1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(elrond);
    }

    @Test
    @DisplayName("A land's mana ability neither draws nor consumes the creature trigger")
    void landAbilityDoesNotConsumeTrigger() {
        addCreatureReady(player1, new ElrondMoonReader());
        harness.addToBattlefield(player1, new Forest());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        addElrondMana(1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent or more than two permanents")
    void rejectsOpponentAndExcessTargets() {
        addCreatureReady(player1, new ElrondMoonReader());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());
        addElrondMana(1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(opponentBear.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A borrowed permanent returns to its owner")
    void returnsUnderOwnersControl() {
        addCreatureReady(player1, new ElrondMoonReader());
        GrizzlyBears bearCard = new GrizzlyBears();
        Permanent bear = addCreatureReady(player1, bearCard);
        gd.stolenCreatures.put(bear.getId(), player2.getId());
        seedLibrary(1);
        addElrondMana(1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard).containsExactly(bearCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).doesNotContain(bearCard);
    }

    @Test
    @DisplayName("The draw resolves before the activated flicker ability")
    void drawResolvesBeforeFlicker() {
        addCreatureReady(player1, new ElrondMoonReader());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addElrondMana(1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);

        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("Activating an opponent's creature ability does not draw for Elrond")
    void opponentsActivationDoesNotDraw() {
        addCreatureReady(player1, new ElrondMoonReader());
        addCreatureReady(player2, new ElrondMoonReader());
        seedLibrary(1);
        harness.setLibrary(player2, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbilityWithMultiTargets(player2, 0, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
    }

    @Test
    @DisplayName("Flickering during an end step waits for the following end step")
    void activationDuringEndStepReturnsNextEndStep() {
        addCreatureReady(player1, new ElrondMoonReader());
        GrizzlyBears bearCard = new GrizzlyBears();
        Permanent bear = addCreatureReady(player1, bearCard);
        seedLibrary(2);
        addElrondMana(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).doesNotContain(bearCard);
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(bearCard);
    }

    @Test
    @DisplayName("Two cards with different owners return in one delayed ability resolution")
    void differentlyOwnedCardsReturnTogether() {
        addCreatureReady(player1, new ElrondMoonReader());
        GrizzlyBears ownedCard = new GrizzlyBears();
        GrizzlyBears borrowedCard = new GrizzlyBears();
        Permanent owned = addCreatureReady(player1, ownedCard);
        Permanent borrowed = addCreatureReady(player1, borrowedCard);
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());
        seedLibrary(1);
        addElrondMana(1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(owned.getId(), borrowed.getId()));
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(ownedCard);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard).contains(borrowedCard);
        resolveAllTriggers();
    }

    private void seedLibrary(int count) {
        harness.setLibrary(player1, IntStream.range(0, count)
                .mapToObj(i -> new Forest()).toList());
    }

    private void addElrondMana(int activations) {
        harness.addMana(player1, ManaColor.COLORLESS, 5 * activations);
        harness.addMana(player1, ManaColor.BLUE, 2 * activations);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
