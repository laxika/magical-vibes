package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurretOgre.class, AirElemental.class})
class TurretOgreTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to each opponent with another creature of power 4 or greater")
    void etbDamagesEachOpponentWithAnotherLargeCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AirElemental());

        castTurretOgre();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB does not trigger without another qualifying creature you control")
    void etbDoesNotTriggerWithoutAnotherQualifyingCreature() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AirElemental());

        castTurretOgre();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void castTurretOgre() {
        harness.castFromHand(player1, new TurretOgre(), "{3}{R}");
        harness.passBothPriorities();
    }

    @Test
    void doesNotTriggerWhenItIsTheOnlyCreature() {
        harness.setLife(player2, 20);

        castTurretOgre();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void anotherCreatureWithPowerThreeDoesNotQualify() {
        var other = harness.addToBattlefieldAndReturn(player1, new TurretOgre());
        other.setPowerModifier(-1);

        castTurretOgre();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void conditionIsRecheckedWhenOtherCreatureLeaves() {
        harness.setLife(player2, 20);
        var other = harness.addToBattlefieldAndReturn(player1, new TurretOgre());
        castTurretOgre();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(other);
        gd.playerGraveyards.get(player1.getId()).add(other.getCard());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    void conditionIsRecheckedWhenOtherCreatureLosesPower() {
        harness.setLife(player2, 20);
        var other = harness.addToBattlefieldAndReturn(player1, new TurretOgre());
        castTurretOgre();
        assertThat(gd.stack).hasSize(1);

        other.setPowerModifier(-1);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    void triggerStillDealsDamageAfterSourceLeaves() {
        harness.setLife(player2, 20);
        var other = harness.addToBattlefieldAndReturn(player1, new TurretOgre());
        castTurretOgre();
        assertThat(gd.stack).hasSize(1);
        var source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != other).findFirst().orElseThrow();

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }
}
