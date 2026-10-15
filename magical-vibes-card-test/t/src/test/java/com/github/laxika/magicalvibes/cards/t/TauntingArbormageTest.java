package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({TauntingArbormage.class, Forest.class})
class TauntingArbormageTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotRequireBlocks() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TauntingArbormage());
        harness.setHand(player1, List.of(new TauntingArbormage()));
        addMana(3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    void kickedRequiresAllAbleCreaturesToBlockTarget() {
        Permanent target = addCreatureReady(player1, new TauntingArbormage());
        Permanent blocker1 = addCreatureReady(player2, new TauntingArbormage());
        Permanent blocker2 = addCreatureReady(player2, new TauntingArbormage());
        harness.setHand(player1, List.of(new TauntingArbormage()));
        addMana(6);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

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
        Permanent target = addCreatureReady(player2, new TauntingArbormage());
        harness.setHand(player1, List.of(new TauntingArbormage()));
        addMana(6);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    void enteringWithoutBeingCastDoesNotRequireBlocks() {
        Permanent target = addCreatureReady(player1, new TauntingArbormage());

        Permanent arbormage = harness.enterBattlefieldAndReturn(player1, new TauntingArbormage());
        resolveAllTriggers();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isFalse();
        assertThat(arbormage.isMustBeBlockedByAllThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedCreatureIsNotRequiredToBlock() {
        Permanent target = addCreatureReady(player1, new TauntingArbormage());
        Permanent blocker = addCreatureReady(player2, new TauntingArbormage());
        Permanent tappedBlocker = addCreatureReady(player2, new TauntingArbormage());
        tappedBlocker.tap();
        harness.setHand(player1, List.of(new TauntingArbormage()));
        addMana(6);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    @Test
    void competingRequirementsAllowBlockerToChooseEitherAttacker() {
        Permanent first = addCreatureReady(player1, new TauntingArbormage());
        Permanent second = addCreatureReady(player1, new TauntingArbormage());
        Permanent blocker = addCreatureReady(player2, new TauntingArbormage());
        harness.setHand(player1, List.of(new TauntingArbormage(), new TauntingArbormage()));
        addMana(6);
        addMana(6);

        harness.castKickedCreature(player1, 0, first.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.castKickedCreature(player1, 0, second.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        first.setAttacking(true);
        second.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, amount - 1);
    }
}
