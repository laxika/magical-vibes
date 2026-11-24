package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfDominaria;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RenderInert.class, Forest.class, GrizzlyBears.class, InvasionOfDominaria.class})
class RenderInertTest extends BaseCardTest {

    @Test
    @DisplayName("Removes up to five counters of mixed kinds, how many of each kind chosen, then draws a card")
    void removesUpToFiveMixedCountersAndDrawsACard() {
        Permanent target = addTargetWithCounters(3, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast(target);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("0", "1", "2", "3");
        harness.handleListChoice(player1, "3");
        choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("0", "1", "2");
        harness.handleListChoice(player1, "2");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("May remove fewer than five counters")
    void mayRemoveFewerThanFiveCounters() {
        Permanent target = addTargetWithCounters(0, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast(target);
        harness.handleListChoice(player1, "1");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only permanents can be targeted")
    void onlyPermanentsCanBeTargeted() {
        harness.setHand(player1, List.of(new RenderInert()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May remove no counters and still draws")
    void mayRemoveZeroCountersAndStillDraw() {
        Permanent target = addTargetWithCounters(2, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast(target);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.handleListChoice(player1, "0");
        harness.handleListChoice(player1, "0");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draws without a question when the target has no counters")
    void drawsWhenTargetHasNoCounters() {
        Permanent target = addTargetWithCounters(0, 0);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast(target);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Render Inert");
    }

    @Test
    @DisplayName("Asks no more once every counter there is chosen, then draws")
    void stopsChoosingWhenAllAvailableCountersHaveBeenRemoved() {
        Permanent target = addTargetWithCounters(0, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast(target);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.handleListChoice(player1, "2");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Render Inert");
    }

    @Test
    @DisplayName("Does nothing, not even the draw, when its only target left before it resolves")
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        Permanent target = addTargetWithCounters(0, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RenderInert()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Render Inert");
    }

    @Test
    @DisplayName("Removing a Siege's last defense counter triggers its defeat ability")
    void removingLastDefenseCounterDefeatsSiege() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfDominaria());
        battle.setCounterCount(CounterType.DEFENSE, 2);

        cast(battle);
        harness.handleListChoice(player1, "2");

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isZero();
        assertThat(gd.stack).anyMatch(entry -> battle.getId().equals(entry.getSourcePermanentId()));
    }

    private Permanent addTargetWithCounters(int plusOneCounters, int chargeCounters) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, plusOneCounters);
        target.setCounterCount(CounterType.CHARGE, chargeCounters);
        return target;
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new RenderInert()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
