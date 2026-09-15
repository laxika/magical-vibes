package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CateranBrute;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.g.GerrardsIrregulars;
import com.github.laxika.magicalvibes.cards.g.GiantCaterpillar;
import com.github.laxika.magicalvibes.cards.w.WallOfGlare;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        RighteousIndignation.class,
        CateranBrute.class,
        GerrardsIrregulars.class,
        GiantCaterpillar.class,
        FreshVolunteers.class,
        WallOfGlare.class
})
class RighteousIndignationTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a black creature gives the blocker +1/+1")
    void blackAttackerBoostsBlocker() {
        Permanent attacker = addCreatureReady(player1, new CateranBrute());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking a red creature gives the blocker +1/+1")
    void redAttackerBoostsBlocker() {
        Permanent attacker = addCreatureReady(player1, new GerrardsIrregulars());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking a nonblack, nonred creature does not boost the blocker")
    void otherColoredAttackerDoesNotBoostBlocker() {
        Permanent attacker = addCreatureReady(player1, new GiantCaterpillar());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blocker gets one boost for each black or red creature it blocks")
    void eachMatchingBlockedAttackerTriggersSeparately() {
        Permanent blackAttacker = addCreatureReady(player1, new CateranBrute());
        blackAttacker.setAttacking(true);
        Permanent redAttacker = addCreatureReady(player1, new GerrardsIrregulars());
        redAttacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WallOfGlare());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GerrardsIrregulars());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }
}
