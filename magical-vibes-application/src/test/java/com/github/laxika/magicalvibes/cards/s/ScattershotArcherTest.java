package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenSquire;
import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScattershotArcher.class, AvenSquire.class, CanyonMinotaur.class})
class ScattershotArcherTest extends BaseCardTest {

    @Test
    void damagesFlyingCreaturesOnBothSidesAndLeavesGroundCreaturesAndPlayersUnharmed() {
        Permanent archer = addCreatureReady(player1, new ScattershotArcher());
        harness.addToBattlefield(player1, new AvenSquire());
        harness.addToBattlefield(player2, new AvenSquire());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new AvenSquire());
        survivor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent ground = harness.addToBattlefieldAndReturn(player2, new CanyonMinotaur());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(archer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(survivor.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Squire");
        harness.assertInGraveyard(player2, "Aven Squire");
        assertThat(countPermanents(player2, "Aven Squire")).isEqualTo(1);
        assertThat(survivor.getMarkedDamage()).isEqualTo(1);
        assertThat(ground.getMarkedDamage()).isZero();
        assertThat(archer.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void canActivateWithoutFlyingCreatures() {
        Permanent archer = addCreatureReady(player1, new ScattershotArcher());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(archer.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Scattershot Archer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ScattershotArcher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent archer = addCreatureReady(player1, new ScattershotArcher());
        archer.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesAfterSourceLeavesAndIncludesCreaturesThatArriveBeforeResolution() {
        Permanent archer = addCreatureReady(player1, new ScattershotArcher());

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(archer);
        gd.playerGraveyards.get(player1.getId()).add(archer.getCard());
        harness.addToBattlefield(player2, new AvenSquire());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aven Squire");
        harness.assertNotOnBattlefield(player2, "Aven Squire");
        assertThat(gd.stack).isEmpty();
    }
}
