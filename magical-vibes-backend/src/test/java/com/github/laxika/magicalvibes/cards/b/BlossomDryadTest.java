package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlossomDryadTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Blossom Dryad has correct activated ability")
    void hasCorrectProperties() {
        BlossomDryad card = new BlossomDryad();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().get(0))
                .isInstanceOf(UntapTargetPermanentEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(PermanentPredicateTargetFilter.class);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a land")
    void activatingPutsOnStack() {
        Permanent dryad = addReadyDryad(player1);
        harness.addToBattlefield(player2, new Forest());

        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Blossom Dryad");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Blossom Dryad")
    void activatingTapsBlossomDryad() {
        Permanent dryad = addReadyDryad(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(dryad.isTapped()).isTrue();
    }

    // ===== Untapping lands =====

    @Test
    @DisplayName("Untaps a tapped land")
    void untapsTappedLand() {
        addReadyDryad(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);
        target.tap();

        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap an already untapped land (no-op)")
    void untapsAlreadyUntappedLand() {
        addReadyDryad(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    // ===== Targeting own lands =====

    @Test
    @DisplayName("Can untap own tapped land")
    void canUntapOwnLand() {
        addReadyDryad(player1);
        harness.addToBattlefield(player1, new Forest());
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    // ===== Invalid targets =====

    @Test
    @DisplayName("Cannot target a non-land creature")
    void cannotTargetNonLandCreature() {
        addReadyDryad(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).get(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BlossomDryad());
        harness.addToBattlefield(player2, new Forest());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Already tapped =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent dryad = addReadyDryad(player1);
        dryad.tap();
        harness.addToBattlefield(player2, new Forest());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target land is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyDryad(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addReadyDryad(Player player) {
        Permanent perm = new Permanent(new BlossomDryad());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
