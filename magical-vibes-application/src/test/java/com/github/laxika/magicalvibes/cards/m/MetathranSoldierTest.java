package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetathranSoldier.class})
class MetathranSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("Metathran Soldier can't be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new MetathranSoldier());
        addCreatureReady(player1, new MetathranSoldier());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Unblocked Metathran Soldier deals combat damage to the defending player")
    void dealsDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new MetathranSoldier());
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
