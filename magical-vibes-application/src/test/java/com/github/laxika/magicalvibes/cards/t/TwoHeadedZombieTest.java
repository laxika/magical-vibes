package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwoHeadedZombie.class, GreenwoodSentinel.class})
class TwoHeadedZombieTest extends BaseCardTest {

    @Test
    @DisplayName("A single creature cannot block Two-Headed Zombie")
    void singleBlockerIsIllegal() {
        Permanent attacker = addCreatureReady(player1, new TwoHeadedZombie());
        addCreatureReady(player2, new GreenwoodSentinel());

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Two creatures can block Two-Headed Zombie")
    void twoBlockersAreLegal() {
        Permanent attacker = addCreatureReady(player1, new TwoHeadedZombie());
        addCreatureReady(player2, new GreenwoodSentinel());
        addCreatureReady(player2, new GreenwoodSentinel());

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.combatBlockOpponentIdsThisTurn.get(attacker.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Two-Headed Zombie may be left unblocked even when two blockers are available")
    void noBlockersAreLegal() {
        Permanent attacker = addCreatureReady(player1, new TwoHeadedZombie());
        addCreatureReady(player2, new GreenwoodSentinel());
        addCreatureReady(player2, new GreenwoodSentinel());

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("More than two creatures can block Two-Headed Zombie")
    void threeBlockersAreLegal() {
        Permanent attacker = addCreatureReady(player1, new TwoHeadedZombie());
        addCreatureReady(player2, new GreenwoodSentinel());
        addCreatureReady(player2, new GreenwoodSentinel());
        addCreatureReady(player2, new GreenwoodSentinel());

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.combatBlockOpponentIdsThisTurn.get(attacker.getId())).hasSize(3);
    }

}
