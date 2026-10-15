package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArmoredWolfRider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MurmuringPhantasm.class, ArmoredWolfRider.class})
class MurmuringPhantasmTest extends BaseCardTest {

    @Test
    void defenderCannotAttackEvenWithoutSummoningSickness() {
        Permanent phantasm = addCreatureReady(player1, new MurmuringPhantasm());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(phantasm.isAttacking()).isFalse();
        assertThat(phantasm.isTapped()).isFalse();
    }

    @Test
    void defenderCanBlockWhileSummoningSick() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player2, new MurmuringPhantasm());
        Permanent attacker = addCreatureReady(player1, new ArmoredWolfRider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(phantasm);
        assertThat(phantasm.getMarkedDamage()).isEqualTo(4);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }
}
