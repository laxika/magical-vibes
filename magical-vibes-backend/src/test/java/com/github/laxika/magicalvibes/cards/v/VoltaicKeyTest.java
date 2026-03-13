package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoltaicKeyTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Voltaic Key has correct activated ability")
    void hasCorrectProperties() {
        VoltaicKey card = new VoltaicKey();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().get(0))
                .isInstanceOf(UntapTargetPermanentEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(PermanentPredicateTargetFilter.class);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting an artifact")
    void activatingPutsOnStack() {
        harness.addToBattlefield(player1, new VoltaicKey());
        AngelsFeather targetCard = new AngelsFeather();
        harness.addToBattlefield(player2, targetCard);
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Voltaic Key");
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Activating ability taps Voltaic Key")
    void activatingTapsVoltaicKey() {
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);

        Permanent voltaicKey = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(voltaicKey.isTapped()).isTrue();
    }

    // ===== Untapping artifacts =====

    @Test
    @DisplayName("Untaps a tapped artifact")
    void untapsTappedArtifact() {
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);
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
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    // ===== Targeting own artifacts =====

    @Test
    @DisplayName("Can untap own tapped artifact")
    void canUntapOwnArtifact() {
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.addToBattlefield(player1, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player1, "Angel's Feather");
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target itself — untaps Voltaic Key after paying tap cost")
    void canTargetItself() {
        harness.addToBattlefield(player1, new VoltaicKey());
        UUID voltaicKeyId = harness.getPermanentId(player1, "Voltaic Key");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, voltaicKeyId);
        harness.passBothPriorities();

        Permanent voltaicKey = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(voltaicKey.isTapped()).isFalse();
    }

    // ===== Invalid targets =====

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    // ===== Costs =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new VoltaicKey());
        Permanent voltaicKey = gd.playerBattlefields.get(player1.getId()).get(0);
        voltaicKey.tap();
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Non-creature artifact is not affected by summoning sickness")
    void notAffectedBySummoningSickness() {
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Voltaic Key is summoning sick by default from addToBattlefield,
        // but non-creature artifacts can still use tap abilities
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
