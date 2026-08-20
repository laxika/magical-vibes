package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tracker.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class, SavannahLions.class})
class TrackerTest extends BaseCardTest {

    @Test
    void fightsSmallerCreatureAndSurvives() {
        Permanent tracker = addReadyTracker(player1);
        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player2.getId()).add(wizard);

        activateTracker(tracker, wizard);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(wizard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(tracker.getId()));
        assertThat(tracker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void fightsEqualCreatureAndBothDie() {
        Permanent tracker = addReadyTracker(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        activateTracker(tracker, bears);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(tracker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        addReadyTracker(player1);
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(forest);

        assertThatThrownBy(() -> {
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.activateAbility(player1, 0, null, forest.getId());
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent tracker = addReadyTracker(player1);
        Permanent lions = new Permanent(new SavannahLions());
        gd.playerBattlefields.get(player2.getId()).add(lions);

        activateTracker(tracker, lions);

        Permanent otherLions = new Permanent(new SavannahLions());
        gd.playerBattlefields.get(player2.getId()).add(otherLions);
        assertThatThrownBy(() -> {
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.activateAbility(player1, 0, null, otherLions.getId());
        }).isInstanceOf(IllegalStateException.class);
    }

    private void activateTracker(Permanent tracker, Permanent target) {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyTracker(Player player) {
        Permanent perm = new Permanent(new Tracker());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
