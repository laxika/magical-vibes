package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UlvenwaldTracker.class, HillGiant.class, GrizzlyBears.class})
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
    @DisplayName("Neither creature deals damage if a target leaves before resolution")
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

    @Test
    @DisplayName("Tracker can fight as the first target even though activating taps it")
    void trackerCanFight() {
        Permanent tracker = addTrackerReady(player1);
        Permanent other = addTrackerReady(player2);
        payMana(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(tracker.getId(), other.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tracker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(other);
        harness.assertInGraveyard(player1, "Ulvenwald Tracker");
        harness.assertInGraveyard(player2, "Ulvenwald Tracker");
    }

    @Test
    @DisplayName("Neither creature fights if the first target changes controllers")
    void noFightWhenFirstTargetChangesControllers() {
        addTrackerReady(player1);
        Permanent mine = addTrackerReady(player1);
        Permanent theirs = addTrackerReady(player2);
        payMana(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(mine);
        gd.playerBattlefields.get(player2.getId()).add(mine);
        harness.passBothPriorities();

        assertThat(mine.getMarkedDamage()).isZero();
        assertThat(theirs.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mine, theirs);
    }

    @Test
    @DisplayName("Ability still resolves if Tracker leaves and both targets remain legal")
    void abilityResolvesWithoutSource() {
        Permanent tracker = addTrackerReady(player1);
        Permanent mine = addTrackerReady(player1);
        Permanent theirs = addTrackerReady(player2);
        payMana(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(tracker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mine);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(theirs);
    }

    @Test
    @DisplayName("Summoning-sick Tracker cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent tracker = addTrackerReady(player1);
        tracker.setSummoningSick(true);
        Permanent mine = addTrackerReady(player1);
        Permanent theirs = addTrackerReady(player2);
        payMana(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tracker.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires both generic and green mana")
    void insufficientManaPreventsActivation() {
        Permanent tracker = addTrackerReady(player1);
        Permanent mine = addTrackerReady(player1);
        Permanent theirs = addTrackerReady(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tracker.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTrackerReady(Player player) {
        return addCreatureReady(player, new UlvenwaldTracker());
    }

    private void payMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
