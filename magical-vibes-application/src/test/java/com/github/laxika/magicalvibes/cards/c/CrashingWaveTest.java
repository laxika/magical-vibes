package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TurtleDuck;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrashingWave.class, TurtleDuck.class})
class CrashingWaveTest extends BaseCardTest {

    @Test
    void waterbendsTapsTargetsAndDistributesCountersAmongTappedOpponentCreatures() {
        Permanent waterbendSourceOne = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent waterbendSourceTwo = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        Permanent alreadyTapped = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        alreadyTapped.tap();
        harness.setHand(player1, List.of(new CrashingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 2, null, null,
                List.of(firstTarget.getId(), secondTarget.getId()), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(waterbendSourceOne.getId(), waterbendSourceTwo.getId()));
        harness.passBothPriorities();

        assertThat(waterbendSourceOne.isTapped()).isTrue();
        assertThat(waterbendSourceTwo.isTapped()).isTrue();
        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 1);

        assertThat(firstTarget.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(secondTarget.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(alreadyTapped.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(waterbendSourceOne.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canChooseXGreaterThanOneHundredWhenEnoughManaIsAvailable() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        recipient.tap();
        harness.setHand(player1, List.of(new CrashingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 101);

        harness.castAndResolveSorcery(player1, 0, 101);
        harness.handleXValueChosen(player1, 3);

        assertThat(recipient.getCounterCount(CounterType.STUN)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Crashing Wave");
    }

    @Test
    void zeroXCanPutAllCountersOnOneAlreadyTappedCreatureAndSkipAnother() {
        Permanent skipped = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        Permanent untapped = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        skipped.tap();
        recipient.tap();
        harness.setHand(player1, List.of(new CrashingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player1, 3);

        assertThat(skipped.getCounterCount(CounterType.STUN)).isZero();
        assertThat(recipient.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(untapped.isTapped()).isFalse();
        assertThat(untapped.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Crashing Wave");
    }

    @Test
    void canPayWaterbendWithManaAndTargetFewerThanXIncludingOwnCreature() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        recipient.tap();
        harness.setHand(player1, List.of(new CrashingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2, List.of(ownTarget.getId()));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);

        assertThat(ownTarget.isTapped()).isTrue();
        assertThat(ownTarget.getCounterCount(CounterType.STUN)).isZero();
        assertThat(recipient.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithoutCounterChoiceWhenNoOpponentCreatureIsTapped() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        harness.setHand(player1, List.of(new CrashingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1, ownTarget.getId());

        assertThat(ownTarget.isTapped()).isTrue();
        assertThat(ownTarget.getCounterCount(CounterType.STUN)).isZero();
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Crashing Wave");
    }

    @Test
    void doesNotDistributeCountersWhenEveryDeclaredTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        Permanent alreadyTapped = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        alreadyTapped.tap();
        harness.setHand(player1, List.of(new CrashingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(alreadyTapped.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Crashing Wave");
    }

    @Test
    void stillTapsAndDistributesCountersWhenOneOfTwoTargetsLeavesBattlefield() {
        Permanent removedTarget = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        Permanent remainingTarget = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        harness.setHand(player1, List.of(new CrashingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2, List.of(removedTarget.getId(), remainingTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removedTarget);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);

        assertThat(remainingTarget.isTapped()).isTrue();
        assertThat(remainingTarget.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Crashing Wave");
    }
}
