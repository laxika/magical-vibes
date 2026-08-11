package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TritonTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Up to two target creatures get +0/+3 and untap")
    void boostsAndUntapsBothTargets() {
        Permanent first = addTappedCreature(player2);
        Permanent second = addTappedCreature(player2);

        castTritonTactics(List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(first.getPowerModifier()).isZero();
        assertThat(first.getToughnessModifier()).isEqualTo(3);
        assertThat(second.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("At the next end of combat, creatures blocked by the targets are tapped and skip untap")
    void tapsAndLocksCreaturesBlockedByTargets() {
        Permanent targetBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        castTritonTactics(List.of(targetBlocker.getId()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceThroughEndOfCombat();

        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        assertThat(targetBlocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The delayed effect tracks both chosen creatures")
    void tracksBothChosenCreatures() {
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        castTritonTactics(List.of(firstTarget.getId(), secondTarget.getId()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        advanceThroughEndOfCombat();

        assertThat(firstAttacker.isTapped()).isTrue();
        assertThat(secondAttacker.isTapped()).isTrue();
        assertThat(firstAttacker.getSkipUntapCount()).isEqualTo(1);
        assertThat(secondAttacker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Targeting an attacker does not affect creatures blocking it")
    void affectsOnlyCreaturesBlockedByTargets() {
        Permanent targetAttacker = addCreatureReady(player1, new GiantSpider());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        targetAttacker.setAttacking(true);

        castTritonTactics(List.of(targetAttacker.getId()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceThroughEndOfCombat();

        assertThat(blocker.isTapped()).isFalse();
        assertThat(blocker.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Only creatures blocked by the chosen creatures are affected")
    void ignoresOtherCombatCreatures() {
        Permanent targetBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent affectedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unaffectedAttacker = addCreatureReady(player1, new GrizzlyBears());
        affectedAttacker.setAttacking(true);
        unaffectedAttacker.setAttacking(true);

        castTritonTactics(List.of(targetBlocker.getId()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        advanceThroughEndOfCombat();

        assertThat(affectedAttacker.isTapped()).isTrue();
        assertThat(affectedAttacker.getSkipUntapCount()).isEqualTo(1);
        assertThat(unaffectedAttacker.isTapped()).isFalse();
        assertThat(unaffectedAttacker.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("The boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = addTappedCreature(player2);

        castTritonTactics(List.of(target.getId()));
        assertThat(target.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new TritonTactics()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTritonTactics(List<UUID> targets) {
        harness.setHand(player1, List.of(new TritonTactics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targets);
        harness.passBothPriorities();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = addCreatureReady(player, new GrizzlyBears());
        perm.tap();
        return perm;
    }

    private void advanceThroughEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
