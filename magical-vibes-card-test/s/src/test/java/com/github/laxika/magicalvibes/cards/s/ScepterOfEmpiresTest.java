package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrownOfEmpires;
import com.github.laxika.magicalvibes.cards.t.ThroneOfEmpires;
import com.github.laxika.magicalvibes.cards.c.ChandraTheFirebrand;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScepterOfEmpires.class, CrownOfEmpires.class, ThroneOfEmpires.class, ChandraTheFirebrand.class})
class ScepterOfEmpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Without both partners the ability deals 1 damage")
    void dealsOneDamageWithoutPartners() {
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        int startingLife = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("With only one partner the ability still deals 1 damage")
    void dealsOneDamageWithOnePartner() {
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        int startingLife = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("With both partners the ability deals 3 damage instead")
    void dealsThreeDamageWithBothPartners() {
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        int startingLife = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 3);
    }

    @Test
    void dealsOneDamageWithOnlyThrone() {
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void opponentsPartnerDoesNotIncreaseDamage() {
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        harness.addToBattlefield(player2, new ThroneOfEmpires());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void checksPartnersAtResolutionWhenPartnerLeaves() {
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfEmpires());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(throne);
        gd.playerGraveyards.get(player1.getId()).add(throne.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void checksPartnersAtResolutionWhenPartnerArrives() {
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void abilityStillDealsThreeDamageAfterScepterLeaves() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new ScepterOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(scepter);
        gd.playerGraveyards.get(player1.getId()).add(scepter.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void canTargetItsControllerAndPaysTapCost() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new ScepterOfEmpires());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        assertThat(scepter.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void dealsOneDamageToPlaneswalkerWithoutPartners() {
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        Permanent chandra = harness.enterBattlefieldAndReturn(player2, new ChandraTheFirebrand());

        harness.activateAbility(player1, 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chandra);
    }

    @Test
    void dealsThreeDamageToPlaneswalkerWithPartners() {
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        Permanent chandra = harness.enterBattlefieldAndReturn(player2, new ChandraTheFirebrand());

        harness.activateAbility(player1, 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(chandra);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(chandra.getCard());
    }
}
