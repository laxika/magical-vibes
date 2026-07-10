package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeyDruidTest extends BaseCardTest {

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a land")
    void activatingPutsOnStack() {
        addReadyDruid(player1);
        harness.addToBattlefield(player2, new Forest());

        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Ley Druid")
    void activatingTapsDruid() {
        Permanent druid = addReadyDruid(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(druid.isTapped()).isTrue();
    }

    // ===== Untapping lands =====

    @Test
    @DisplayName("Untaps a tapped land")
    void untapsTappedLand() {
        addReadyDruid(player1);
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
        addReadyDruid(player1);
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
        addReadyDruid(player1);
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
        addReadyDruid(player1);
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
        harness.addToBattlefield(player1, new LeyDruid());
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
        Permanent druid = addReadyDruid(player1);
        druid.tap();
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
        addReadyDruid(player1);
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

    private Permanent addReadyDruid(Player player) {
        Permanent perm = new Permanent(new LeyDruid());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
