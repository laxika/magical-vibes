package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidalKraken.class, DeadlyInsect.class})
class TidalKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Tidal Kraken cannot be blocked by a ground creature")
    void cannotBeBlocked() {
        addCreatureReady(player2, new DeadlyInsect());

        Permanent attacker = addCreatureReady(player1, new TidalKraken());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Unblocked Tidal Kraken deals 6 damage to defending player")
    void dealsDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new TidalKraken());
        attacker.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}
