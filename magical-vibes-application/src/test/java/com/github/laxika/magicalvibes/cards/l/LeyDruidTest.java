package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeyDruid.class, Forest.class, GrizzlyBears.class})
class LeyDruidTest extends BaseCardTest {

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a land")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new LeyDruid());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Ley Druid")
    void activatingTapsDruid() {
        Permanent druid = addCreatureReady(player1, new LeyDruid());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(druid.isTapped()).isTrue();
    }

    // ===== Untapping lands =====

    @Test
    @DisplayName("Untaps a tapped land")
    void untapsTappedLand() {
        addCreatureReady(player1, new LeyDruid());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();

        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap an already untapped land (no-op)")
    void untapsAlreadyUntappedLand() {
        addCreatureReady(player1, new LeyDruid());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    // ===== Targeting own lands =====

    @Test
    @DisplayName("Can untap own tapped land")
    void canUntapOwnLand() {
        addCreatureReady(player1, new LeyDruid());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    // ===== Invalid targets =====

    @Test
    @DisplayName("Cannot target a non-land creature")
    void cannotTargetNonLandCreature() {
        addCreatureReady(player1, new LeyDruid());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new LeyDruid());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Already tapped =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent druid = addCreatureReady(player1, new LeyDruid());
        druid.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target land is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new LeyDruid());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

}
