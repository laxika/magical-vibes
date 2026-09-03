package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BayFalcon.class, DarkBanishing.class, TeekasDragon.class})
class TeekasDragonTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 4 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setAttacking(true);
        addReadyBlocker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 4 grants +4/+4 until end of turn")
    void twoBlockersGivesPlusFour() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setAttacking(true);
        addReadyBlocker(player2);
        addReadyBlocker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(4);
        assertThat(dragon.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("With three blockers Rampage 4 grants +8/+8 until end of turn")
    void threeBlockersGivesPlusEight() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setAttacking(true);
        addReadyBlocker(player2);
        addReadyBlocker(player2);
        addReadyBlocker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(8);
        assertThat(dragon.getToughnessModifier()).isEqualTo(8);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The rampage bonus wears off at end of turn")
    void bonusWearsOffAtEndOfTurn() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setAttacking(true);
        addReadyBlocker(player2);
        addReadyBlocker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(4);
        assertThat(dragon.getToughnessModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With no blockers remaining when rampage resolves, no negative bonus is applied")
    void noNegativeBonusWhenBlockerLeavesBeforeResolution() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setAttacking(true);
        Permanent blocker = addReadyBlocker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, blocker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bay Falcon");
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    private Permanent addReadyDragon(Player player) {
        return addCreatureReady(player, new TeekasDragon());
    }

    private Permanent addReadyBlocker(Player player) {
        return addCreatureReady(player, new BayFalcon());
    }
}
