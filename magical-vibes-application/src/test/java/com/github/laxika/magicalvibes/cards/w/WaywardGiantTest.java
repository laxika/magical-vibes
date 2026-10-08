package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaywardGiant.class, PrakhataPillarBug.class})
class WaywardGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Wayward Giant can be left unblocked even when a blocker is available")
    void noBlockersAreLegal() {
        Permanent attacker = addCreatureReady(player1, new WaywardGiant());
        addCreatureReady(player2, new PrakhataPillarBug());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.combatBlockOpponentIdsThisTurn).doesNotContainKey(attacker.getId());
    }

    @Test
    @DisplayName("More than two creatures can block Wayward Giant")
    void threeBlockersAreLegal() {
        Permanent attacker = addCreatureReady(player1, new WaywardGiant());
        addCreatureReady(player2, new PrakhataPillarBug());
        addCreatureReady(player2, new PrakhataPillarBug());
        addCreatureReady(player2, new PrakhataPillarBug());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0), new BlockerAssignment(2, 0)));

        assertThat(gd.combatBlockOpponentIdsThisTurn.get(attacker.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A single creature cannot block Wayward Giant")
    void singleBlockerIsIllegal() {
        Permanent attacker = addCreatureReady(player1, new WaywardGiant());
        addCreatureReady(player2, new PrakhataPillarBug());

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Two creatures can block Wayward Giant")
    void twoBlockersAreLegal() {
        Permanent attacker = addCreatureReady(player1, new WaywardGiant());
        addCreatureReady(player2, new PrakhataPillarBug());
        addCreatureReady(player2, new PrakhataPillarBug());

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.combatBlockOpponentIdsThisTurn.get(attacker.getId())).hasSize(2);
    }
}
