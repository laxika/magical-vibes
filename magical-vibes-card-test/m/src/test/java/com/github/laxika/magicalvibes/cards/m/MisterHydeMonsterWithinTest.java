package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MisterHydeMonsterWithin.class, GrizzlyBears.class, Forest.class})
class MisterHydeMonsterWithinTest extends BaseCardTest {

    @Test
    void putsPlusOnePlusOneCounterOnMisterHyde() {
        MisterHydeMonsterWithin hydeCard = new MisterHydeMonsterWithin();
        var hyde = harness.addToBattlefieldAndReturn(player1, hydeCard);

        resolveUpkeepTrigger("Put a +1/+1 counter on Mister Hyde.");

        assertThat(hyde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void removesCounterFromChosenCreatureAndDraws() {
        var hyde = harness.addToBattlefieldAndReturn(player1, new MisterHydeMonsterWithin());
        var bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of());
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        resolveUpkeepTrigger("Remove a counter from a creature you control. If you do, draw a card.");

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bear.getId());
        harness.handlePermanentChosen(player1, bear.getId());

        assertThat(bear.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerHands.get(player1.getId()).stream().map(card -> card.getId()).toList())
                .containsExactly(libraryCard.getId());
        assertThat(hyde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void choosesModeBeforePlayersCanRespondToUpkeepTrigger() {
        harness.addToBattlefield(player1, new MisterHydeMonsterWithin());

        advanceToUpkeep(player1);

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly(
                "Put a +1/+1 counter on Mister Hyde.",
                "Remove a counter from a creature you control. If you do, draw a card.");
        harness.handleListChoice(player1, "Put a +1/+1 counter on Mister Hyde.");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void allowsChoosingCounterTypeBeforeRemovingOneAndDrawing() {
        var hyde = harness.addToBattlefieldAndReturn(player1, new MisterHydeMonsterWithin());
        hyde.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        hyde.setCounterCount(CounterType.STUN, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveUpkeepTrigger("Remove a counter from a creature you control. If you do, draw a card.");
        harness.handlePermanentChosen(player1, hyde.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(hyde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(hyde.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void removesExactlyOneCounterFromMisterHydeAndDraws() {
        var hyde = harness.addToBattlefieldAndReturn(player1, new MisterHydeMonsterWithin());
        hyde.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of());
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        resolveUpkeepTrigger("Remove a counter from a creature you control. If you do, draw a card.");
        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(hyde.getId());
        harness.handlePermanentChosen(player1, hyde.getId());

        assertThat(hyde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void doesNotDrawWhenOnlyOpposingCreaturesAndOwnNoncreaturesHaveCounters() {
        harness.addToBattlefield(player1, new MisterHydeMonsterWithin());
        var opponentHyde = harness.addToBattlefieldAndReturn(player2, new MisterHydeMonsterWithin());
        opponentHyde.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of());
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        resolveUpkeepTrigger("Remove a counter from a creature you control. If you do, draw a card.");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(opponentHyde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(forest.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        var hyde = harness.addToBattlefieldAndReturn(player1, new MisterHydeMonsterWithin());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(hyde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void resolveUpkeepTrigger(String mode) {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
    }
}
