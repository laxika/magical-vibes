package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodlandPatrol.class})
class WoodlandPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Woodland Patrol untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent patrol = addCreatureReady(player1, new WoodlandPatrol());

        declareAttackers(List.of(0));

        assertThat(patrol.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Woodland Patrol to attack")
    void tappedPatrolCannotAttack() {
        Permanent patrol = addCreatureReady(player1, new WoodlandPatrol());
        patrol.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(patrol.isTapped()).isTrue();
        assertThat(patrol.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not bypass summoning sickness")
    void summoningSickPatrolCannotAttack() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new WoodlandPatrol());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(patrol.isTapped()).isFalse();
        assertThat(patrol.isAttacking()).isFalse();
    }
}
