package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UlvenwaldTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Both creatures deal damage equal to their power to each other")
    void creaturesFight() {
        Permanent tracker = addTrackerReady(player1);
        Permanent mine = addCreatureReady(player1, new HillGiant());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        payMana(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));
        harness.passBothPriorities();

        assertThat(tracker.isTapped()).isTrue();
        assertThat(mine.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(theirs);
    }

    @Test
    @DisplayName("Second target may be a creature you control")
    void secondTargetCanBeOwnCreature() {
        addTrackerReady(player1);
        Permanent first = addCreatureReady(player1, new HillGiant());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        payMana(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(second);
    }

    @Test
    @DisplayName("First target must be a creature you control")
    void firstTargetMustBeControlled() {
        addTrackerReady(player1);
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        Permanent mine = addCreatureReady(player1, new HillGiant());
        payMana(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(theirs.getId(), mine.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The same creature cannot be chosen twice (\"another\")")
    void targetsMustBeDifferent() {
        addTrackerReady(player1);
        Permanent mine = addCreatureReady(player1, new HillGiant());
        payMana(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(mine.getId(), mine.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if a target leaves the battlefield before resolution")
    void fizzlesWhenTargetLeaves() {
        addTrackerReady(player1);
        Permanent mine = addCreatureReady(player1, new HillGiant());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        payMana(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(theirs);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(mine.getMarkedDamage()).isZero();
    }

    private Permanent addTrackerReady(Player player) {
        Permanent perm = new Permanent(new UlvenwaldTracker());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void payMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
