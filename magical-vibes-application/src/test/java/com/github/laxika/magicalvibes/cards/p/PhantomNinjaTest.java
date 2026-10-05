package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantomNinja.class, GrizzlyBears.class})
class PhantomNinjaTest extends BaseCardTest {

    @Test
    @DisplayName("Phantom Ninja cannot be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new GrizzlyBears());

        Permanent attacker = addCreatureReady(player1, new PhantomNinja());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Unblocked Phantom Ninja deals combat damage to the defending player")
    void dealsCombatDamageWhenUnblocked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new PhantomNinja());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
