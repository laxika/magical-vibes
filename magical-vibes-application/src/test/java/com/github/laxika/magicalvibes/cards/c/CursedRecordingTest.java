package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CursedRecording.class, LightningBolt.class, GrizzlyBears.class})
class CursedRecordingTest extends BaseCardTest {

    @Test
    void putsTimeCounterOnCastingInstantOrSorcery() {
        Permanent recording = addRecording();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(recording.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void removesTimeCountersAndDealsTwentyDamageAtSevenCounters() {
        Permanent recording = addRecording();
        recording.setCounterCount(CounterType.TIME, 6);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(recording.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
    }

    @Test
    void doesNotTriggerForCreatureSpells() {
        Permanent recording = addRecording();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(recording.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    void tappingItCopiesTheNextInstantOrSorceryCast() {
        Permanent recording = addRecording();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(recording.isTapped()).isTrue();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().startsWith("Copy Lightning Bolt"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    private Permanent addRecording() {
        return harness.addToBattlefieldAndReturn(player1, new CursedRecording());
    }
}
