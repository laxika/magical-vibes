package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerathiBerserker.class, GrizzlyBears.class})
class AerathiBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Aerathi Berserker gets no rampage bonus")
    void oneBlockerGivesNoBonus() {
        Permanent berserker = addBerserkerAndDeclareBlockers(1);

        assertThat(berserker.getEffectivePower()).isEqualTo(2);
        assertThat(berserker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("With two blockers Aerathi Berserker gets +3/+3 until end of turn")
    void twoBlockersGivePlusThreePlusThree() {
        Permanent berserker = addBerserkerAndDeclareBlockers(2);

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
        assertThat(berserker.getToughnessModifier()).isEqualTo(3);
        assertThat(berserker.getEffectivePower()).isEqualTo(5);
        assertThat(berserker.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("With three blockers Aerathi Berserker gets +6/+6 until end of turn")
    void threeBlockersGivePlusSixPlusSix() {
        Permanent berserker = addBerserkerAndDeclareBlockers(3);

        assertThat(berserker.getPowerModifier()).isEqualTo(6);
        assertThat(berserker.getToughnessModifier()).isEqualTo(6);
        assertThat(berserker.getEffectivePower()).isEqualTo(8);
        assertThat(berserker.getEffectiveToughness()).isEqualTo(10);
    }

    @Test
    @DisplayName("If unblocked Aerathi Berserker gets no rampage bonus")
    void unblockedGivesNoBonus() {
        Permanent berserker = addCreatureReady(player1, new AerathiBerserker());
        berserker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(berserker.getPowerModifier()).isZero();
        assertThat(berserker.getToughnessModifier()).isZero();
    }

    private Permanent addBerserkerAndDeclareBlockers(int blockerCount) {
        Permanent berserker = addCreatureReady(player1, new AerathiBerserker());
        berserker.setAttacking(true);
        for (int i = 0; i < blockerCount; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }

        List<BlockerAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < blockerCount; i++) {
            assignments.add(new BlockerAssignment(i, 0));
        }
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
        return berserker;
    }
}
