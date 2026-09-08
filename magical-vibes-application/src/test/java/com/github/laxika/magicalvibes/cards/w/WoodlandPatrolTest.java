package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WoodlandPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Woodland Patrol untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent patrol = addCreatureReady(player1, new WoodlandPatrol());

        declareAttackers(List.of(0));

        assertThat(patrol.isTapped()).isFalse();
    }
}
