package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwathcutterGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Swathcutter Giant deals 1 damage to each defending creature")
    void attackTriggerDamagesDefendingCreatures() {
        addCreatureReady(player1, new SwathcutterGiant());
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent defendingHillGiant = addCreatureReady(player2, new HillGiant());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingBears.getMarkedDamage()).isEqualTo(1);
        assertThat(defendingHillGiant.getMarkedDamage()).isEqualTo(1);
        assertThat(ownBears.getMarkedDamage()).isZero();
    }
}
