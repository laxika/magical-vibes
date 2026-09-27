package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.l.LumengridWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bloodscent.class, Forest.class, LeoninSkyhunter.class, LumengridWarden.class})
class BloodscentTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Bloodscent requires all able creatures to block the target")
    void resolvingSetsLureRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LumengridWarden());

        castAndResolveBloodscent(target);

        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    @DisplayName("All creatures able to block the target must be declared as blockers")
    void allAbleCreaturesMustBlock() {
        Permanent target = addCreatureReady(player1, new LumengridWarden());
        Permanent blocker1 = addCreatureReady(player2, new LumengridWarden());
        Permanent blocker2 = addCreatureReady(player2, new LumengridWarden());

        castAndResolveBloodscent(target);

        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not required to block the target")
    void tappedCreaturesAreNotRequiredToBlock() {
        Permanent target = addCreatureReady(player1, new LumengridWarden());
        Permanent untapped = addCreatureReady(player2, new LumengridWarden());
        Permanent tapped = addCreatureReady(player2, new LumengridWarden());
        tapped.tap();

        castAndResolveBloodscent(target);

        target.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A creature unable to block the target is not required to block")
    void unableCreatureIsNotRequiredToBlock() {
        Permanent target = addCreatureReady(player1, new LeoninSkyhunter());
        Permanent blocker = addCreatureReady(player2, new LumengridWarden());

        castAndResolveBloodscent(target);

        target.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Bloodscent cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareBloodscent();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The requirement wears off at end of turn")
    void requirementWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LumengridWarden());

        castAndResolveBloodscent(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    private void castAndResolveBloodscent(Permanent target) {
        prepareBloodscent();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareBloodscent() {
        harness.setHand(player1, List.of(new Bloodscent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
