package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistralCharger.class, GrizzlyBears.class})
class MistralChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(charger)));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(charger);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
