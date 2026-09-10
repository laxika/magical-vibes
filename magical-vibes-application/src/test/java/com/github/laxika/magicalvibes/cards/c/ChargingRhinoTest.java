package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BayouDragonfly;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChargingRhino.class, BayouDragonfly.class})
class ChargingRhinoTest extends BaseCardTest {

    @Test
    @DisplayName("Charging Rhino can be blocked by one creature")
    void canBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new ChargingRhino());
        attacker.setAttacking(true);

        addCreatureReady(player2, new BayouDragonfly());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Charging Rhino cannot be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new ChargingRhino());
        attacker.setAttacking(true);

        addCreatureReady(player2, new BayouDragonfly());
        addCreatureReady(player2, new BayouDragonfly());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Each Charging Rhino can be blocked by one creature")
    void eachAttackerCanBeBlockedByOneCreature() {
        Permanent firstAttacker = addCreatureReady(player1, new ChargingRhino());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new ChargingRhino());
        secondAttacker.setAttacking(true);

        Permanent firstBlocker = addCreatureReady(player2, new BayouDragonfly());
        Permanent secondBlocker = addCreatureReady(player2, new BayouDragonfly());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
