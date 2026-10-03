package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CourierHawk.class, BorosRecruit.class})
class CourierHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking does not tap Courier Hawk")
    void attackingDoesNotTapCourierHawk() {
        Permanent hawk = addCreatureReady(player1, new CourierHawk());

        declareAttackers(List.of(0));

        assertThat(hawk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Courier Hawk cannot be blocked by a creature without flying")
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player1, new CourierHawk());
        addCreatureReady(player2, new BorosRecruit());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new CourierHawk());
        Permanent blocker = addCreatureReady(player2, new CourierHawk());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockGroundCreature() {
        addCreatureReady(player1, new BorosRecruit());
        Permanent hawk = addCreatureReady(player2, new CourierHawk());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(hawk.isBlocking()).isTrue();
    }

    @Test
    void vigilanceDoesNotAllowAttackingWhileTapped() {
        Permanent hawk = addCreatureReady(player1, new CourierHawk());
        hawk.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(hawk.isAttacking()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowAttackingWithSummoningSickness() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new CourierHawk());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(hawk.isAttacking()).isFalse();
    }
}
