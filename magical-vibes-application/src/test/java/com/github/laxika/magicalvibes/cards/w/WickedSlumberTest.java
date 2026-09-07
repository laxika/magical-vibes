package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WickedSlumber.class, GrizzlyBears.class})
class WickedSlumberTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two creatures and can put both stun counters on one target")
    void putsBothStunCountersOnOneTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, first.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(first.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Can split the two stun counters between the targets")
    void splitsStunCountersBetweenTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(first.getId(), second.getId()));

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts both stun counters on the only target when one creature is targeted")
    void putsBothCountersOnOneTargetWithoutChoice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Puts both counters on the remaining legal target")
    void remainingLegalTargetGetsBothCounters() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WickedSlumber()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, List.of(removed.getId(), remaining.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(remaining.isTapped()).isTrue();
        assertThat(remaining.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Convoke can pay for the spell")
    void convokesThreeCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WickedSlumber()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId(), thirdConvokeCreature.getId()));
        harness.passBothPriorities();

        assertThat(firstConvokeCreature.isTapped()).isTrue();
        assertThat(secondConvokeCreature.isTapped()).isTrue();
        assertThat(thirdConvokeCreature.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new WickedSlumber()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
