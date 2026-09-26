package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SamiteArcher.class)
class SamiteArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next damage to a target creature")
    void preventsNextDamageToTargetCreature() {
        Permanent archer = addCreatureReady(player1, new SamiteArcher());
        Permanent target = addCreatureReady(player2, new SamiteArcher());
        addCreatureReady(player1, new SamiteArcher());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Samite Archer");
        assertThat(archer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevents the next damage to a target player")
    void preventsNextDamageToTargetPlayer() {
        addCreatureReady(player1, new SamiteArcher());
        addCreatureReady(player1, new SamiteArcher());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals 1 damage to a target player")
    void dealsDamageToTargetPlayer() {
        Permanent archer = addCreatureReady(player1, new SamiteArcher());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(archer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void dealsDamageToTargetCreature() {
        Permanent archer = addCreatureReady(player1, new SamiteArcher());
        Permanent target = addCreatureReady(player2, new SamiteArcher());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Samite Archer");
        assertThat(archer.isTapped()).isTrue();
    }
}
