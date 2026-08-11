package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BelovedChaplainTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from creatures prevents combat damage")
    void protectionFromCreaturesPreventsCombatDamage() {
        Permanent chaplain = harness.addToBattlefieldAndReturn(player2, new BelovedChaplain());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(chaplain.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Beloved Chaplain");
    }
}
