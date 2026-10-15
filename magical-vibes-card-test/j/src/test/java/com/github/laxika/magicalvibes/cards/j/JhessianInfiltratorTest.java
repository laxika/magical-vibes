package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.k.KathariScreecher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JhessianInfiltrator.class, CylianElf.class, KathariScreecher.class})
class JhessianInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Jhessian Infiltrator cannot be blocked by a ground creature")
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player2, new CylianElf());

        Permanent atkPerm = addCreatureReady(player1, new JhessianInfiltrator());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Unblocked Jhessian Infiltrator deals 2 damage to defending player")
    void dealsTwoDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new JhessianInfiltrator());
        atkPerm.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Flying does not allow a creature to block Jhessian Infiltrator")
    void cannotBeBlockedByFlyingCreature() {
        addCreatureReady(player2, new KathariScreecher());
        Permanent attacker = addCreatureReady(player1, new JhessianInfiltrator());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Jhessian Infiltrator can block an ordinary attacker")
    void canBlockAnOrdinaryAttacker() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new CylianElf());
        attacker.setAttacking(true);
        addCreatureReady(player2, new JhessianInfiltrator());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Cylian Elf");
        harness.assertInGraveyard(player2, "Jhessian Infiltrator");
    }
}
