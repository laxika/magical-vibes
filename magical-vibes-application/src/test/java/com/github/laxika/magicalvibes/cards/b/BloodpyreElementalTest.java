package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.j.JungleWeaver;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodpyreElemental.class, CylianElf.class, JungleWeaver.class})
class BloodpyreElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature, killing a 2/2")
    void dealsFourDamageKillingSmallCreature() {
        harness.addToBattlefield(player1, new BloodpyreElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cylian Elf");
    }

    @Test
    @DisplayName("A creature with toughness greater than 4 survives")
    void largeCreatureSurvives() {
        harness.addToBattlefield(player1, new BloodpyreElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JungleWeaver());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Jungle Weaver");
    }

    @Test
    @DisplayName("Bloodpyre Elemental is sacrificed as a cost of the ability")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new BloodpyreElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Bloodpyre Elemental");
    }

    @Test
    void dealsExactlyFourDamageAfterSourceIsSacrificed() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BloodpyreElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JungleWeaver());
        source.setSummoningSick(true);
        source.tap();

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Bloodpyre Elemental");
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Jungle Weaver");
    }

    @Test
    void canTargetAnotherCreatureYouControl() {
        harness.addToBattlefield(player1, new BloodpyreElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BloodpyreElemental());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void canTargetItselfButSacrificeMakesTargetIllegal() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BloodpyreElemental());

        harness.activateAbility(player1, 0, 0, null, source.getId());

        harness.assertInGraveyard(player1, "Bloodpyre Elemental");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BloodpyreElemental());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bloodpyre Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BloodpyreElemental());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bloodpyre Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithAbilityAlreadyOnStack() {
        harness.addToBattlefield(player1, new BloodpyreElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BloodpyreElemental());
        harness.activateAbility(player1, 0, 0, null, second.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, second.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(second);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotTargetPlayerAndDoesNotPaySacrificeCost() {
        harness.addToBattlefield(player1, new BloodpyreElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bloodpyre Elemental");
        assertThat(gd.stack).isEmpty();
    }
}
