package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaphaelNinjaDestroyer.class, Shock.class, GrizzlyBears.class})
class RaphaelNinjaDestroyerTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana equal to damage dealt and preserves it through a phase change")
    void addsPersistentRedManaEqualToDamageDealt() {
        harness.addToBattlefield(player2, new RaphaelNinjaDestroyer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID raphaelId = harness.getPermanentId(player2, "Raphael, Ninja Destroyer");
        harness.castInstant(player1, 0, raphaelId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getPersistentMana(ManaColor.RED)).isEqualTo(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Must be blocked when an able blocker exists")
    void mustBeBlockedIfAble() {
        Permanent raphael = addCreatureReady(player1, new RaphaelNinjaDestroyer());
        raphael.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

}
