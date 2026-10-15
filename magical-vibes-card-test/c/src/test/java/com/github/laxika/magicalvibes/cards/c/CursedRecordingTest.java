package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CursedRecording.class, LightningBolt.class, GrizzlyBears.class, Pyroclasm.class})
class CursedRecordingTest extends BaseCardTest {

    @Test
    void putsTimeCounterOnCastingInstantOrSorcery() {
        Permanent recording = addRecording();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(recording.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void removesTimeCountersAndDealsTwentyDamageAtSevenCounters() {
        Permanent recording = addRecording();
        recording.setCounterCount(CounterType.TIME, 6);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

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

    @Test
    void sorceryAddsOneTimeCounter() {
        Permanent recording = addRecording();
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(recording.getCounterCount(CounterType.TIME)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void removesAllTimeCountersAboveSevenButKeepsOtherCounters() {
        Permanent recording = addRecording();
        recording.setCounterCount(CounterType.TIME, 8);
        recording.setCounterCount(CounterType.CHARGE, 2);
        harness.setLife(player1, 40);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(recording.getCounterCount(CounterType.TIME)).isZero();
        assertThat(recording.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    @Test
    void copiedSpellResolvesWithoutAddingCountersAndOnlyNextSpellIsCopied() {
        Permanent recording = addRecording();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(recording.getCounterCount(CounterType.TIME)).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 11);
        assertThat(recording.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanChooseDifferentTargetWithoutChangingOriginalTarget() {
        Permanent recording = addRecording();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, first.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(recording.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void resolvedCopyAbilitySurvivesRecordingLeavingBattlefield() {
        Permanent recording = addRecording();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(recording);
        harness.setGraveyard(player1, List.of(recording.getCard()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unusedCopyAbilityExpiresAtEndOfTurn() {
        addRecording();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureSpellDoesNotConsumeCopyAndNextSorceryIsCopied() {
        Permanent recording = addRecording();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GrizzlyBears(), new Pyroclasm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst().orElseThrow();
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(recording.getCounterCount(CounterType.TIME)).isZero();

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(recording.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsInstantNeitherAddsCounterNorConsumesCopy() {
        Permanent recording = addRecording();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        assertThat(recording.getCounterCount(CounterType.TIME)).isZero();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(recording.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void instantCastBeforeActivatedAbilityResolvesIsNotCopied() {
        Permanent recording = addRecording();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(recording.getCounterCount(CounterType.TIME)).isEqualTo(1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 11);
        assertThat(recording.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    private Permanent addRecording() {
        return harness.addToBattlefieldAndReturn(player1, new CursedRecording());
    }
}
