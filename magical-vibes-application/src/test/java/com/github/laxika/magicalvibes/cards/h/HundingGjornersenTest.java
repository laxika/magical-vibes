package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HundingGjornersen.class, GrizzlyBears.class})
class HundingGjornersenTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rampage 1 grants no bonus")
    void oneBlockerGivesNothing() {
        Permanent hunding = addAttackingHunding();
        addBlockers(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hunding.getPowerModifier()).isZero();
        assertThat(hunding.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With three blockers Rampage 1 grants +2/+2 until end of turn")
    void threeBlockersGivesPlusTwo() {
        Permanent hunding = addAttackingHunding();
        addBlockers(3);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        harness.passBothPriorities();

        assertThat(hunding.getPowerModifier()).isEqualTo(2);
        assertThat(hunding.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent hunding = addAttackingHunding();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(hunding.getPowerModifier()).isZero();
        assertThat(hunding.getToughnessModifier()).isZero();
    }

    private Permanent addAttackingHunding() {
        Permanent permanent = addCreatureReady(player1, new HundingGjornersen());
        permanent.setAttacking(true);
        return permanent;
    }

    private void addBlockers(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }
    }
}
