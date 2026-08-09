package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("Resolving Kill Switch taps all other artifacts but not nonartifacts")
    void resolvingTapsOtherArtifacts() {
        Permanent killSwitch = addReadyKillSwitch(player1);
        Permanent ownArtifact = addReadyArtifact(player1);
        Permanent opposingArtifact = addReadyArtifact(player2);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(killSwitch.isTapped()).isTrue();
        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(opposingArtifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other artifacts remain tapped until Kill Switch untaps")
    void lockEndsWhenKillSwitchUntaps() {
        Permanent killSwitch = addReadyKillSwitch(player1);
        Permanent opposingArtifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        assertThat(killSwitch.isTapped()).isTrue();
        assertThat(opposingArtifact.isTapped()).isTrue();

        advanceToNextTurn(player2);
        assertThat(killSwitch.isTapped()).isFalse();
        assertThat(opposingArtifact.isTapped()).isTrue();

        advanceToNextTurn(player1);
        assertThat(opposingArtifact.isTapped()).isFalse();
    }

    private Permanent addReadyKillSwitch(Player player) {
        Permanent permanent = new Permanent(new KillSwitch());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent permanent = new Permanent(new AngelsFeather());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
