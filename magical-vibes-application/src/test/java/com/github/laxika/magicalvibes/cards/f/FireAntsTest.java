package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FireAntsTest extends BaseCardTest {

    private Permanent addReadyAnts() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new FireAnts());
        ants.setSummoningSick(false);
        return ants;
    }

    @Test
    @DisplayName("Deals 1 damage to each other creature without flying, sparing flyers")
    void damagesOnlyOtherNonFlyers() {
        Permanent ants = addReadyAnts();
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fire Ants");
        assertThat(ants.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        addReadyAnts();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
