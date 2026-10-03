package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrazingWhiptail;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlossomDryad.class, Forest.class, GrazingWhiptail.class})
class BlossomDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a land")
    void activatingPutsOnStack() {
        addReadyDryad(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(BlossomDryad.class);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Blossom Dryad")
    void activatingTapsBlossomDryad() {
        Permanent dryad = addReadyDryad(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(dryad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped land")
    void untapsTappedLand() {
        addReadyDryad(player1);
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
        addReadyDryad(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped land")
    void canUntapOwnLand() {
        addReadyDryad(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-land creature")
    void cannotTargetNonLandCreature() {
        addReadyDryad(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrazingWhiptail());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BlossomDryad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent dryad = addReadyDryad(player1);
        dryad.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Fizzles if target land is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyDryad(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Ability resolves even if Blossom Dryad leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent dryad = addReadyDryad(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(dryad);
        gd.playerGraveyards.get(player1.getId()).add(dryad.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyDryad(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BlossomDryad());
        perm.setSummoningSick(false);
        return perm;
    }
}
