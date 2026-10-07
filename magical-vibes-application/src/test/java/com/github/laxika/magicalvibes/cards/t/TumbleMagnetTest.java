package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GraftedExoskeleton;
import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.cards.t.TemperedSteel;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TumbleMagnet.class, AlphaTyrranax.class, GraftedExoskeleton.class, TemperedSteel.class})
class TumbleMagnetTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new TumbleMagnet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent magnet = findMagnet(player1);
        assertThat(magnet.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingTargetingCreaturePutsOnStack() {
        addReadyMagnet(player1);
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Tumble Magnet")
    void activatingTapsMagnet() {
        Permanent magnet = addReadyMagnet(player1);
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(magnet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTargetCreature() {
        addReadyMagnet(player1);
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolution leaves the charge counter spent during activation")
    void resolutionDoesNotSpendAnotherCounter() {
        Permanent magnet = addReadyMagnet(player1);
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(magnet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can tap target artifact")
    void canTapTargetArtifact() {
        addReadyMagnet(player1);
        Permanent targetArtifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(targetArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyMagnet(player1);
        Permanent enchantment = addReadyEnchantment(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Can activate multiple times with enough counters (untapping between)")
    void canActivateMultipleTimes() {
        Permanent magnet = addReadyMagnet(player1);
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());

        // First activation
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        magnet.untap();
        target.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        magnet.untap();
        target.untap();

        // Third activation
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(magnet.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with 0 charge counters")
    void cannotActivateWithNoCounters() {
        Permanent magnet = addReadyMagnet(player1);
        magnet.setCounterCount(CounterType.CHARGE, 0);
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent magnet = addReadyMagnet(player1);
        magnet.tap();
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can tap own creature")
    void canTapOwnCreature() {
        addReadyMagnet(player1);
        Permanent ownCreature = addCreatureReady(player1, new AlphaTyrranax());

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyMagnet(player1);
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Charge counter is paid immediately, before the target is tapped")
    void paysCounterDuringActivation() {
        Permanent magnet = addReadyMagnet(player1);
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(magnet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(magnet.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(magnet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate on the turn it enters and target itself")
    void canActivateImmediatelyTargetingItself() {
        harness.setHand(player1, List.of(new TumbleMagnet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent magnet = findMagnet(player1);

        harness.activateAbility(player1, 0, null, magnet.getId());
        harness.passBothPriorities();

        assertThat(magnet.isTapped()).isTrue();
        assertThat(magnet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature is a legal target")
    void canTargetTappedCreature() {
        Permanent magnet = addReadyMagnet(player1);
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(magnet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability still resolves after Tumble Magnet leaves the battlefield")
    void resolvesWithoutSource() {
        addReadyMagnet(player1);
        Permanent target = addCreatureReady(player2, new AlphaTyrranax());
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyMagnet(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TumbleMagnet());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.CHARGE, 3);
        return perm;
    }

    private Permanent findMagnet(Player player) {
        return findPermanent(player, "Tumble Magnet");
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GraftedExoskeleton());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TemperedSteel());
    }
}
