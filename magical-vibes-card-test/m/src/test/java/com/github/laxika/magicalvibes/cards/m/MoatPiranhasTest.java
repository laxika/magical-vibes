package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoatPiranhas.class, GreenwoodSentinel.class})
class MoatPiranhasTest extends BaseCardTest {

    @Test
    void cannotAttack() {
        addCreatureReady(player1, new MoatPiranhas());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void canBlockWhileSummoningSickAndDealCombatDamage() {
        addCreatureReady(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new MoatPiranhas());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Moat Piranhas");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
    }
}
