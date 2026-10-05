package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InkfathomInfiltrator.class, GrizzlyBears.class})
class InkfathomInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Inkfathom Infiltrator cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new InkfathomInfiltrator());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Inkfathom Infiltrator cannot be declared as a blocker")
    void cannotBlock() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player2, new InkfathomInfiltrator());
        infiltrator.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Inkfathom Infiltrator can attack and deal combat damage")
    void canAttackAndDealCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new InkfathomInfiltrator());
        attacker.setSummoningSick(false);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
