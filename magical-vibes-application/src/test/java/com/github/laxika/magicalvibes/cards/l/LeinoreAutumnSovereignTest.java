package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeinoreAutumnSovereign.class, Forest.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class LeinoreAutumnSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on up to one creature without drawing when coven is not met")
    void putsCounterWithoutCoven() {
        addCreatureReady(player1, new LeinoreAutumnSovereign());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws a card after the counter is placed when coven is met")
    void drawsWithCovenAfterCounterPlacement() {
        addCreatureReady(player1, new LeinoreAutumnSovereign());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("May decline the target and still draw when coven is met")
    void mayDeclineTargetAndStillDrawWithCoven() {
        addCreatureReady(player1, new LeinoreAutumnSovereign());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(elf.getId(), bear.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Offers only creatures you control as the optional target")
    void targetIsControlledCreatureOnly() {
        addCreatureReady(player1, new LeinoreAutumnSovereign());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The counter can create coven during resolution")
    void counterCreatesCoven() {
        addCreatureReady(player1, new LeinoreAutumnSovereign());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The counter can break coven before the draw is checked")
    void counterBreaksCoven() {
        Permanent target = addCreatureReady(player1, new LeinoreAutumnSovereign());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the target without coven does not draw")
    void decliningWithoutCovenDoesNotDraw() {
        Permanent leinore = addCreatureReady(player1, new LeinoreAutumnSovereign());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(leinore.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent leinore = addCreatureReady(player1, new LeinoreAutumnSovereign());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(leinore.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
