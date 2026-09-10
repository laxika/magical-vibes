package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TauntingArbormage.class, GrizzlyBears.class, Forest.class})
class TauntingArbormageTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotRequireBlocks() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TauntingArbormage()));
        addMana(3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    void kickedRequiresAllAbleCreaturesToBlockTarget() {
        Permanent target = addReadyCreature(player1);
        Permanent blocker1 = addReadyCreature(player2);
        Permanent blocker2 = addReadyCreature(player2);
        harness.setHand(player1, List.of(new TauntingArbormage()));
        addMana(6);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();

        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    void kickedTargetMustBeACreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TauntingArbormage()));
        addMana(6);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requirementWearsOffAtEndOfTurn() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new TauntingArbormage()));
        addMana(6);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, amount - 1);
    }
}
