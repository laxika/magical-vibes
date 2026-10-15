package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbominationOfLlanowar.class, LlanowarElves.class, GrizzlyBears.class})
class AbominationOfLlanowarTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance leaves Abomination untapped when it attacks")
    void attackingDoesNotTapAbomination() {
        Permanent abomination = addCreatureReady(player1, new AbominationOfLlanowar());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(abomination.isAttacking()).isTrue();
        assertThat(abomination.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void cannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new AbominationOfLlanowar());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two blockers")
    void canBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new AbominationOfLlanowar());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The characteristic ability works in the graveyard and counts itself there")
    void powerAndToughnessAreDefinedInGraveyard() {
        AbominationOfLlanowar abomination = new AbominationOfLlanowar();
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(abomination, new LlanowarElves()));
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setGraveyard(player2, List.of(new LlanowarElves()));

        assertThat(gqs.getEffectiveCardPower(gd, abomination)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, abomination)).isEqualTo(3);
    }

    @Test
    @DisplayName("The characteristic ability works in hand without counting itself as a controlled Elf")
    void powerAndToughnessAreDefinedInHand() {
        AbominationOfLlanowar abomination = new AbominationOfLlanowar();
        harness.setHand(player1, List.of(abomination));
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        assertThat(gqs.getEffectiveCardPower(gd, abomination)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, abomination)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power and toughness count own Elves on the battlefield and in the graveyard")
    void powerAndToughnessCountOwnElvesInBothZones() {
        Permanent abomination = addCreatureReady(player1, new AbominationOfLlanowar());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setGraveyard(player1, List.of(
                new LlanowarElves(), new LlanowarElves(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new LlanowarElves()));

        // The Abomination itself and one other controlled Elf, plus two Elf cards in its graveyard.
        assertThat(gqs.getEffectivePower(gd, abomination)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, abomination)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power and toughness update when the counted zones change")
    void powerAndToughnessUpdateDynamically() {
        Permanent abomination = addCreatureReady(player1, new AbominationOfLlanowar());

        assertThat(gqs.getEffectivePower(gd, abomination)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, abomination)).isEqualTo(1);

        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, abomination)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, abomination)).isEqualTo(3);
    }
}
