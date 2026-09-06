package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IvoryGargoyle;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
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

@CardUsed({BalduvianWarMakers.class, IvoryGargoyle.class, StormCrow.class})
class BalduvianWarMakersTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 1 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent warMakers = addCreatureReady(player1, new BalduvianWarMakers());
        warMakers.setAttacking(true);
        addCreatureReady(player2, new StormCrow());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(warMakers.getPowerModifier()).isZero();
        assertThat(warMakers.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Removing the only blocker before Rampage resolves gives no bonus")
    void removingOnlyBlockerBeforeRampageResolvesGivesNoBonus() {
        Permanent warMakers = addCreatureReady(player1, new BalduvianWarMakers());
        warMakers.setAttacking(true);
        addCreatureReady(player2, new IvoryGargoyle());
        harness.addMana(player2, ManaColor.WHITE, 5);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Ivory Gargoyle");
        harness.passBothPriorities();

        assertThat(warMakers.getPowerModifier()).isZero();
        assertThat(warMakers.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With three blockers Rampage 1 grants +2/+2 until end of turn")
    void threeBlockersGivesPlusTwo() {
        Permanent warMakers = addCreatureReady(player1, new BalduvianWarMakers());
        warMakers.setAttacking(true);
        addCreatureReady(player2, new StormCrow());
        addCreatureReady(player2, new StormCrow());
        addCreatureReady(player2, new StormCrow());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(warMakers.getPowerModifier()).isEqualTo(2);
        assertThat(warMakers.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent warMakers = addCreatureReady(player1, new BalduvianWarMakers());
        warMakers.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(warMakers.getPowerModifier()).isZero();
        assertThat(warMakers.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Rampage 1's bonus wears off at end of turn")
    void bonusWearsOffAtEndOfTurn() {
        Permanent warMakers = addCreatureReady(player1, new BalduvianWarMakers());
        warMakers.setAttacking(true);
        addCreatureReady(player2, new StormCrow());
        addCreatureReady(player2, new StormCrow());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(warMakers.getPowerModifier()).isEqualTo(1);
        assertThat(warMakers.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warMakers.getPowerModifier()).isZero();
        assertThat(warMakers.getToughnessModifier()).isZero();
    }
}
