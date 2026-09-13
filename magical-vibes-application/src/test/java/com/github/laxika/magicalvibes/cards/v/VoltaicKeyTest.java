package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoltaicKey.class, AngelsFeather.class, RuneclawBear.class, Juggernaut.class})
class VoltaicKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting an artifact")
    void activatingPutsOnStack() {
        Permanent voltaicKey = harness.addToBattlefieldAndReturn(player1, new VoltaicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        UUID targetId = target.getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(voltaicKey.getId());
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Activating ability taps Voltaic Key")
    void activatingTapsVoltaicKey() {
        Permanent voltaicKey = harness.addToBattlefieldAndReturn(player1, new VoltaicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        UUID targetId = target.getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(voltaicKey.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped artifact")
    void untapsTappedArtifact() {
        harness.addToBattlefield(player1, new VoltaicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        UUID targetId = target.getId();
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap an already untapped artifact (no-op)")
    void untapsAlreadyUntappedArtifact() {
        harness.addToBattlefield(player1, new VoltaicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        UUID targetId = target.getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped artifact")
    void canUntapOwnArtifact() {
        harness.addToBattlefield(player1, new VoltaicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        UUID targetId = target.getId();
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target itself — untaps Voltaic Key after paying tap cost")
    void canTargetItself() {
        Permanent voltaicKey = harness.addToBattlefieldAndReturn(player1, new VoltaicKey());
        UUID voltaicKeyId = voltaicKey.getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, voltaicKeyId);
        harness.passBothPriorities();

        assertThat(voltaicKey.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new VoltaicKey());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        UUID creatureId = creature.getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Can target an artifact creature")
    void canTargetArtifactCreature() {
        harness.addToBattlefield(player1, new VoltaicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        UUID targetId = target.getId();
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new VoltaicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        UUID targetId = target.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent voltaicKey = harness.addToBattlefieldAndReturn(player1, new VoltaicKey());
        voltaicKey.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        UUID targetId = target.getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Non-creature artifact is not affected by summoning sickness")
    void notAffectedBySummoningSickness() {
        harness.addToBattlefield(player1, new VoltaicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        UUID targetId = target.getId();
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Voltaic Key is summoning sick by default from addToBattlefield,
        // but non-creature artifacts can still use tap abilities
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Fizzles if target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new VoltaicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        UUID targetId = target.getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
