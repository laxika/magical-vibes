package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArdentMilitia;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TolarianDrake.class, ArdentMilitia.class})
class TolarianDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out during its controller's untap step and phases in during the next one")
    void phasesOutAndInDuringControllerUntapSteps() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new TolarianDrake());

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(drake);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(drake);

        advanceToUpkeep(player2);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(drake);

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(drake);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(drake);
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking it")
    void flyingPreventsNonFlyingBlocker() {
        Permanent drake = addCreatureReady(player1, new TolarianDrake());
        Permanent blocker = addCreatureReady(player2, new ArdentMilitia());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drake);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
