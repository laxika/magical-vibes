package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoldenBear;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WildGriffin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlluringScent.class, GoldenBear.class, Plains.class, WildGriffin.class})
class AlluringScentTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving sets the must-be-blocked-by-all flag on the target")
    void resolvingSetsFlag() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenBear());
        castAlluringScent(target.getId());

        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Resolving sets only the by-all flag, not the weaker must-be-blocked-if-able one")
    void resolvingDoesNotSetTheIfAbleFlag() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenBear());
        castAlluringScent(target.getId());

        assertThat(target.isMustBeBlockedThisTurn()).isFalse();
        assertThat(target.isMustAttackThisTurn()).isFalse();
        assertThat(target.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Flag wears off at end of turn")
    void flagWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenBear());
        castAlluringScent(target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    @DisplayName("All able creatures must block the affected attacker")
    void allAbleCreaturesMustBlock() {
        Permanent attacker = addCreatureReady(player2, new GoldenBear());
        attacker.setAttacking(true);
        Permanent blocker1 = addCreatureReady(player1, new GoldenBear());
        Permanent blocker2 = addCreatureReady(player1, new GoldenBear());
        castAlluringScent(attacker.getId());

        prepareDeclareBlockers(player2);

        // Only one blocker assigned â€” illegal, both must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block")
    void tappedCreaturesNotForcedToBlock() {
        Permanent attacker = addCreatureReady(player2, new GoldenBear());
        attacker.setAttacking(true);
        Permanent untapped = addCreatureReady(player1, new GoldenBear());
        Permanent tapped = addCreatureReady(player1, new GoldenBear());
        tapped.tap();
        castAlluringScent(attacker.getId());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A creature unable to block the target is not forced to block")
    void unableCreatureIsNotForcedToBlock() {
        Permanent attacker = addCreatureReady(player2, new WildGriffin());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new GoldenBear());
        castAlluringScent(attacker.getId());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareAlluringScent();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAlluringScent(UUID targetId) {
        prepareAlluringScent();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void prepareAlluringScent() {
        harness.setHand(player1, List.of(new AlluringScent()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
