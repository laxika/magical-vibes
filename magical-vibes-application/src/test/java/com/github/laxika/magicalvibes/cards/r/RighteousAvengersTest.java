package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RighteousAvengers.class, Plains.class, GrizzlyBears.class})
class RighteousAvengersTest extends BaseCardTest {

    @Test
    @DisplayName("Righteous Avengers can't be blocked when defending player controls a Plains")
    void cannotBeBlockedWhenDefenderControlsPlains() {
        harness.addToBattlefield(player2, new Plains());

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent attackerPerm = addCreatureReady(player1, new RighteousAvengers());
        attackerPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Righteous Avengers can be blocked when defending player controls no Plains")
    void canBeBlockedWhenDefenderControlsNoPlains() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent attackerPerm = addCreatureReady(player1, new RighteousAvengers());
        attackerPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
