package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfDominaria;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EventidesShadow.class, Forest.class, GrizzlyBears.class, InvasionOfDominaria.class})
class EventidesShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Removes chosen counters from any permanents, then draws and loses life for them")
    void removesCountersDrawsAndLosesLife() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ownCreature.setCounterCount(CounterType.CHARGE, 1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.LORE, 1);

        castEventidesShadow();
        answerRemovals(
                new Removal(ownCreature.getId(), CounterType.PLUS_ONE_PLUS_ONE, 1),
                new Removal(ownCreature.getId(), CounterType.CHARGE, 1),
                new Removal(opponentCreature.getId(), CounterType.LORE, 1));

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.LORE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Choosing zero for every counter removes none and produces no draw or life loss")
    void canChooseNoCounters() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castEventidesShadow();
        answerRemovals();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prompt when no permanent has counters")
    void noPromptWithoutCounters() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        castEventidesShadow();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each permanent and kind is asked once, and the counters come off together after the last answer")
    void removingAllCountersCompletesResolution() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        land.setCounterCount(CounterType.CHARGE, 1);

        castEventidesShadow();
        answerOne(new Removal(first.getId(), CounterType.PLUS_ONE_PLUS_ONE, 1));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        answerRemovals(
                new Removal(second.getId(), CounterType.PLUS_ONE_PLUS_ONE, 1),
                new Removal(land.getId(), CounterType.CHARGE, 1));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Removing a Siege's last defense counter triggers its defeat ability")
    void removingLastDefenseCounterDefeatsSiege() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfDominaria());
        battle.setCounterCount(CounterType.DEFENSE, 2);

        castEventidesShadow();
        answerRemovals(new Removal(battle.getId(), CounterType.DEFENSE, 2));

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isZero();
        assertThat(gd.stack).anyMatch(entry -> battle.getId().equals(entry.getSourcePermanentId()));
    }

    private void castEventidesShadow() {
        harness.castFromHand(player1, new EventidesShadow(), "{4}{B}");
        harness.passBothPriorities();
    }

    private record Removal(UUID permanentId, CounterType counterType, int amount) {
    }

    /** Answers the asked removal question with the listed amount for its permanent and kind (zero if unlisted). */
    private void answerOne(Removal... removals) {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        ChoiceContext.MoveCountersAmountChoice context = (ChoiceContext.MoveCountersAmountChoice) choice.context();
        ChoiceContext.CounterMoveSlot slot = context.slots().get(context.index());
        assertThat(slot.toPermanentId()).isNull();
        int amount = 0;
        for (Removal removal : removals) {
            if (removal.permanentId().equals(slot.fromPermanentId()) && removal.counterType() == slot.counterType()) {
                amount = removal.amount();
            }
        }
        harness.handleListChoice(player1, Integer.toString(amount));
    }

    /** Answers every removal question in turn. */
    private void answerRemovals(Removal... removals) {
        while (gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null
                && gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()
                instanceof ChoiceContext.MoveCountersAmountChoice) {
            answerOne(removals);
        }
    }
}
