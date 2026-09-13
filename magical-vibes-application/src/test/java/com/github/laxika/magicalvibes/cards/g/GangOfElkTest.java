package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({GangOfElk.class, GrizzlyBears.class, Unsummon.class})
class GangOfElkTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Gang of Elk gets +2/+2 until end of turn")
    void oneBlockerGivesPlusTwo() {
        Permanent gang = addCreatureReady(player1, new GangOfElk());
        gang.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gang.getPowerModifier()).isEqualTo(2);
        assertThat(gang.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("With two blockers Gang of Elk gets +4/+4 until end of turn")
    void twoBlockersGivesPlusFour() {
        Permanent gang = addCreatureReady(player1, new GangOfElk());
        gang.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(gang.getPowerModifier()).isEqualTo(4);
        assertThat(gang.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The bonus counts blockers remaining when the trigger resolves")
    void bonusCountsBlockersAtResolution() {
        Permanent gang = addCreatureReady(player1, new GangOfElk());
        gang.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        Permanent bouncedBlocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, bouncedBlocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gang.getPowerModifier()).isEqualTo(2);
        assertThat(gang.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent gang = addCreatureReady(player1, new GangOfElk());
        gang.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gang.getPowerModifier()).isZero();
        assertThat(gang.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The blocking bonus wears off at end of turn")
    void blockingBonusWearsOffAtEndOfTurn() {
        Permanent gang = addCreatureReady(player1, new GangOfElk());
        gang.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gang.getPowerModifier()).isEqualTo(2);
        assertThat(gang.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gang.getPowerModifier()).isZero();
        assertThat(gang.getToughnessModifier()).isZero();
    }
}
