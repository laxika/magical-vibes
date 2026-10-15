package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BolaSlinger;
import com.github.laxika.magicalvibes.cards.c.ConsumingAetherborn;
import com.github.laxika.magicalvibes.cards.s.SigiledSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorShieldHoplite.class, SigiledSentinel.class, ConsumingAetherborn.class, BolaSlinger.class})
class MirrorShieldHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a backup ability and may keep its target")
    void copiesBackupAbility() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ConsumingAetherborn());
        addMirrorShieldHoplite();
        castSigiledSentinel();

        resolveBackupTarget(bears);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Copies a backup ability with a new target")
    void copiesBackupAbilityWithNewTarget() {
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new ConsumingAetherborn());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new ConsumingAetherborn());
        addMirrorShieldHoplite();
        castSigiledSentinel();

        resolveBackupTarget(firstBears);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(firstBears.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(secondBears.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Copies only the first backup ability each turn")
    void copiesOnlyOnceEachTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ConsumingAetherborn());
        addMirrorShieldHoplite();
        harness.setHand(player1, List.of(new SigiledSentinel(), new SigiledSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        castSigiledSentinelFromHand();
        resolveBackupTarget(bears);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        castSigiledSentinelFromHand();
        resolveBackupTarget(bears);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not copy backup targeting only an opponent's creature")
    void doesNotCopyBackupTargetingOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ConsumingAetherborn());
        addMirrorShieldHoplite();
        castSigiledSentinel();

        resolveBackupTarget(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Each Hoplite independently copies the same backup ability")
    void multipleHoplitesCopyIndependently() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ConsumingAetherborn());
        addMirrorShieldHoplite();
        addMirrorShieldHoplite();
        castSigiledSentinel();

        resolveBackupTarget(target);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A copied backup may target an opponent's creature")
    void copyCanTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ConsumingAetherborn());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new ConsumingAetherborn());
        addMirrorShieldHoplite();
        castSigiledSentinel();

        resolveBackupTarget(target);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opponentTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentTarget.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A backup creature's unrelated attack trigger does not trigger Hoplite")
    void doesNotCopyUnrelatedTriggerFromBackupCreature() {
        addMirrorShieldHoplite();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ConsumingAetherborn());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new BolaSlinger());
        attacker.setSummoningSick(false);

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    private void addMirrorShieldHoplite() {
        harness.addToBattlefield(player1, new MirrorShieldHoplite());
    }

    private void castSigiledSentinel() {
        harness.castFromHand(player1, new SigiledSentinel(), "{2}{W}");
        harness.passBothPriorities();
    }

    private void castSigiledSentinelFromHand() {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveBackupTarget(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
