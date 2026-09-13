package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrongholdZeppelin.class, DefiantFalcon.class, StrongholdBiologist.class})
class StrongholdZeppelinTest extends BaseCardTest {

    @Test
    void canBlockCreatureWithFlying() {
        Permanent zeppelin = addCreatureReady(player2, new StrongholdZeppelin());
        addCreatureReady(player1, new DefiantFalcon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(zeppelin.isBlocking()).isTrue();
    }

    @Test
    void cannotBlockCreatureWithoutFlying() {
        addCreatureReady(player2, new StrongholdZeppelin());
        addCreatureReady(player1, new StrongholdBiologist());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }
}
