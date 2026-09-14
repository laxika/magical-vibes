package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FlowstoneCrusher;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({KillSwitch.class, FlowstoneCrusher.class})
class KillSwitchTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Kill Switch taps it and puts its ability on the stack")
    void activatingAbility() {
        Permanent killSwitch = addReadyKillSwitch(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(killSwitch.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Activating Kill Switch requires two mana")
    void requiresTwoMana() {
        Permanent killSwitch = addReadyKillSwitch(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(killSwitch.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving Kill Switch taps all other artifacts but not nonartifacts")
    void resolvingTapsOtherArtifacts() {
        Permanent killSwitch = addReadyKillSwitch(player1);
        Permanent ownArtifact = addReadyKillSwitch(player1);
        Permanent opposingArtifact = addReadyKillSwitch(player2);
        Permanent creature = addCreatureReady(player2, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(killSwitch.isTapped()).isTrue();
        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(opposingArtifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Already-tapped artifacts are locked by Kill Switch")
    void locksAlreadyTappedArtifacts() {
        Permanent killSwitch = addReadyKillSwitch(player1);
        Permanent opposingArtifact = addReadyKillSwitch(player2);
        opposingArtifact.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(killSwitch.isTapped()).isTrue();
        assertThat(opposingArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Artifacts entering after resolution are not locked")
    void artifactsEnteringAfterResolutionAreNotLocked() {
        Permanent killSwitch = addReadyKillSwitch(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent opposingArtifact = addReadyKillSwitch(player2);
        advanceToUpkeep(player2);

        assertThat(killSwitch.isTapped()).isTrue();
        assertThat(opposingArtifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untapping Kill Switch before resolution prevents the untap lock")
    void untappingBeforeResolutionPreventsLock() {
        Permanent killSwitch = addReadyKillSwitch(player1);
        Permanent opposingArtifact = addReadyKillSwitch(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        killSwitch.untap();
        harness.passBothPriorities();
        killSwitch.tap();

        advanceToUpkeep(player2);

        assertThat(opposingArtifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other artifacts remain tapped until Kill Switch untaps")
    void lockEndsWhenKillSwitchUntaps() {
        Permanent killSwitch = addReadyKillSwitch(player1);
        Permanent opposingArtifact = addReadyKillSwitch(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        assertThat(killSwitch.isTapped()).isTrue();
        assertThat(opposingArtifact.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(killSwitch.isTapped()).isFalse();
        assertThat(opposingArtifact.isTapped()).isTrue();

        advanceToUpkeep(player2);
        assertThat(opposingArtifact.isTapped()).isFalse();
    }

    private Permanent addReadyKillSwitch(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new KillSwitch());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
