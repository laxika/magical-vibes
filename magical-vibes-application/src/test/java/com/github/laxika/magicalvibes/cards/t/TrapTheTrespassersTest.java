package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HumbleDefector;
import com.github.laxika.magicalvibes.cards.l.LightningGreaves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrapTheTrespassers.class, HumbleDefector.class, LightningGreaves.class})
class TrapTheTrespassersTest extends BaseCardTest {

    @Test
    void putsOneStunCounterOnEachCreatureChosenByOnePlayerAndTapsThem() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        castTrap();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void putsTwoStunCountersOnCreatureReceivingBothVotes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        harness.addToBattlefield(player2, new HumbleDefector());
        castTrap();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void automaticallyVotesWhenOnlyOneCreatureCanBeChosen() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        castTrap();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void resolvesWithoutVotesWhenOnlyTheCasterControlsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HumbleDefector());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LightningGreaves());
        castTrap();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ownCreature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(equipment.getCounterCount(CounterType.STUN)).isZero();
        assertThat(equipment.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Trap the Trespassers");
    }

    @Test
    void bothPlayersMustChooseCreaturesOutsideTheCastersControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HumbleDefector());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LightningGreaves());
        castTrap();

        assertThat(activeVote().validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(activeVote().validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(ownCreature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(first.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void waitsForAllVotesBeforeApplyingCountersAndLeavesUnvotedCreaturesAlone() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        Permanent unvoted = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        castTrap();

        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        assertThat(target.isTapped()).isFalse();
        assertThat(unvoted.getCounterCount(CounterType.STUN)).isZero();
        assertThat(unvoted.isTapped()).isFalse();
        harness.handleMultiplePermanentsChosen(player2, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(target.isTapped()).isTrue();
        assertThat(unvoted.getCounterCount(CounterType.STUN)).isZero();
        assertThat(unvoted.isTapped()).isFalse();
    }

    @Test
    void canVoteForCreatureWithShroud() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        harness.addToBattlefield(player2, new HumbleDefector());
        Permanent greaves = harness.addToBattlefieldAndReturn(player2, new LightningGreaves());
        greaves.setAttachedTo(target.getId());
        castTrap();

        assertThat(activeVote().validIds()).contains(target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void addsStunCountersToAlreadyTappedCreatureAndReplacesSuccessiveUntaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        target.tap();
        target.setCounterCount(CounterType.STUN, 1);
        castTrap();

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(target.isTapped()).isTrue();
        for (int remaining = 2; remaining >= 0; remaining--) {
            harness.performUntapStep(player2);
            assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(remaining);
            assertThat(target.isTapped()).isTrue();
        }
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    private void castTrap() {
        harness.castFromHand(player1, new TrapTheTrespassers(), "{2}{U}");
        harness.passBothPriorities();
    }

    private PendingInteraction.MultiPermanentChoice activeVote() {
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        return choice;
    }
}
