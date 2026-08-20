package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynchronizedChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Distributes counters and grants both keywords to creatures with counters")
    void distributesCountersAndGrantsKeywordsToCounteredCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent alreadyCountered = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        alreadyCountered.setCounterCount(CounterType.LOYALTY, 1);
        Permanent withoutCounters = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(first.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(second.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(second.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(alreadyCountered.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(alreadyCountered.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(withoutCounters.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(withoutCounters.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Counters and keywords wear off at end of turn only for the keyword grant")
    void keywordsWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Harmonize taps a creature to reduce the cost and exiles the spell")
    void harmonizeTapsCreatureAndExilesSpell() {
        Card spell = new SynchronizedCharge();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.getGameService().playFlashbackSpell(gd, player1, 0, null, null, List.of(creature.getId()),
                List.of(), null, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Synchronized Charge"));
    }
}
