package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BarricadeBreaker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhirapurOsprey.class, BarricadeBreaker.class})
class GhirapurOspreyTest extends BaseCardTest {

    @Test
    @DisplayName("Ground creatures cannot block Ghirapur Osprey, but flying creatures can")
    void flyingRestrictsBlockers() {
        Permanent osprey = addCreatureReady(player1, new GhirapurOsprey());
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new BarricadeBreaker());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new GhirapurOsprey());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bls.canBlockAttacker(gd, groundBlocker, osprey,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, osprey,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Ghirapur Osprey can block a ground creature")
    void canBlockGroundCreature() {
        Permanent attacker = addCreatureReady(player1, new BarricadeBreaker());
        Permanent osprey = harness.addToBattlefieldAndReturn(player2, new GhirapurOsprey());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bls.canBlockAttacker(gd, osprey, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
