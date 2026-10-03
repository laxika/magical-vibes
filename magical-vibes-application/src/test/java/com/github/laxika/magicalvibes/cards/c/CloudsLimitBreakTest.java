package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudsLimitBreak.class, GrizzlyBears.class, Island.class})
class CloudsLimitBreakTest extends BaseCardTest {

    @Test
    @DisplayName("Cross-Slash destroys a target tapped creature")
    void crossSlashDestroysTargetTappedCreature() {
        Permanent target = tappedCreature(player2);
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, target, 2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor);
    }

    @Test
    @DisplayName("Blade Beam destroys tapped creatures controlled by different players")
    void bladeBeamDestroysTappedCreaturesWithDifferentControllers() {
        Permanent ownTarget = tappedCreature(player1);
        Permanent opposingTarget = tappedCreature(player2);
        Permanent untapped = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1, 3, ownTarget.getId(), opposingTarget.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownTarget);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingTarget);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(untapped);
    }

    @Test
    @DisplayName("Blade Beam rejects two targets with the same controller")
    void bladeBeamRejectsTargetsWithSameController() {
        Permanent first = tappedCreature(player2);
        Permanent second = tappedCreature(player2);

        harness.setHand(player1, List.of(new CloudsLimitBreak()));
        addMana(3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Omnislash destroys all tapped creatures and leaves untapped creatures alone")
    void omnislashDestroysAllTappedCreatures() {
        Permanent ownTarget = tappedCreature(player1);
        Permanent opposingTarget = tappedCreature(player2);
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(2, 6);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownTarget);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingTarget);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cloud's Limit Break cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent validTarget = tappedCreature(player2);
        Permanent untappedTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CloudsLimitBreak()));
        addMana(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, untappedTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(validTarget, untappedTarget);
    }

    @Test
    @DisplayName("Blade Beam can be cast without targets")
    void bladeBeamCanHaveNoTargets() {
        Permanent survivor = tappedCreature(player2);

        cast(1, 3);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor);
        harness.assertInGraveyard(player1, "Cloud's Limit Break");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cross-Slash does not destroy a creature that untaps before resolution")
    void crossSlashTargetUntapsBeforeResolution() {
        Permanent target = tappedCreature(player2);
        harness.setHand(player1, List.of(new CloudsLimitBreak()));
        addMana(2);
        harness.castInstant(player1, 0, 0, target.getId());

        target.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertInGraveyard(player1, "Cloud's Limit Break");
    }

    @Test
    @DisplayName("Blade Beam still destroys its legal target when another target untaps")
    void bladeBeamResolvesForRemainingLegalTarget() {
        Permanent ownTarget = tappedCreature(player1);
        Permanent opposingTarget = tappedCreature(player2);
        harness.setHand(player1, List.of(new CloudsLimitBreak()));
        addMana(3);
        harness.castModalInstant(player1, 0, 1, List.of(ownTarget.getId(), opposingTarget.getId()));

        ownTarget.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownTarget);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingTarget);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Omnislash leaves tapped noncreature permanents alone")
    void omnislashDoesNotDestroyTappedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        land.tap();
        Permanent target = tappedCreature(player2);

        cast(2, 6);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land).doesNotContain(target);
    }
    private Permanent tappedCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.tap();
        return creature;
    }

    private void cast(int mode, int totalMana, UUID... targets) {
        harness.setHand(player1, List.of(new CloudsLimitBreak()));
        addMana(totalMana);
        if (mode == 0) {
            harness.castInstant(player1, 0, mode, targets[0]);
        } else {
            harness.castModalInstant(player1, 0, mode, List.of(targets));
        }
        harness.passBothPriorities();
    }

    private void cast(int mode, Permanent target, int totalMana) {
        cast(mode, totalMana, target.getId());
    }

    private void addMana(int totalMana) {
        int white = modeRequiresExtraWhite(totalMana) ? 2 : 1;
        harness.addMana(player1, ManaColor.WHITE, white);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - white);
    }

    private boolean modeRequiresExtraWhite(int totalMana) {
        return totalMana == 6;
    }
}
