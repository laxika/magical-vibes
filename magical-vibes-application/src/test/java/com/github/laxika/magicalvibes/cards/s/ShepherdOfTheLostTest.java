package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShepherdOfTheLostTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent shepherd = addCreatureReady(player1, new ShepherdOfTheLost());
        shepherd.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("First strike lets Shepherd of the Lost survive an equal-power blocker")
    void firstStrikeDealsDamageBeforeAnEqualPowerBlocker() {
        Permanent shepherd = addCreatureReady(player1, new ShepherdOfTheLost());
        shepherd.setAttacking(true);

        AirElemental blockerCard = new AirElemental();
        blockerCard.setPower(3);
        blockerCard.setToughness(3);
        addCreatureReady(player2, blockerCard);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shepherd of the Lost");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Vigilance keeps Shepherd of the Lost untapped after attacking")
    void vigilanceKeepsItUntappedWhenAttacking() {
        Permanent shepherd = addCreatureReady(player1, new ShepherdOfTheLost());

        declareAttackers(List.of(0));

        assertThat(shepherd.isTapped()).isFalse();
    }
}
