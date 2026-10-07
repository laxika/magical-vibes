package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrownOfEmpires;
import com.github.laxika.magicalvibes.cards.s.ScepterOfEmpires;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThroneOfEmpires.class, CrownOfEmpires.class, ScepterOfEmpires.class})
class ThroneOfEmpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Without both partners the ability creates one Soldier token")
    void createsOneTokenWithoutPartners() {
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("With only one partner the ability still creates one Soldier token")
    void createsOneTokenWithOnePartner() {
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("With both partners the ability creates five Soldier tokens instead")
    void createsFiveTokensWithBothPartners() {
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(5);
    }

    @Test
    void opponentPartnersDoNotGiveBonus() {
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        harness.addToBattlefield(player2, new CrownOfEmpires());
        harness.addToBattlefield(player2, new ScepterOfEmpires());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    void partnersAcquiredBeforeResolutionGiveBonus() {
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.addToBattlefield(player1, new ScepterOfEmpires());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(5);
    }

    @Test
    void partnerLostBeforeResolutionRemovesBonus() {
        harness.addToBattlefield(player1, new ThroneOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new ScepterOfEmpires());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(scepter);
        gd.playerGraveyards.get(player1.getId()).add(scepter.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    void abilityResolvesAfterThroneLeavesBattlefield() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfEmpires());
        harness.addToBattlefield(player1, new CrownOfEmpires());
        harness.addToBattlefield(player1, new ScepterOfEmpires());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(throne.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(throne);
        gd.playerGraveyards.get(player1.getId()).add(throne.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(5);
    }
}
