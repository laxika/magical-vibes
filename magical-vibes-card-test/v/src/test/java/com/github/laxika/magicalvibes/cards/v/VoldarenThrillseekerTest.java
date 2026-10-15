package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BolaSlinger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoldarenThrillseeker.class, BolaSlinger.class})
class VoldarenThrillseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts two +1/+1 counters on another creature and grants the sacrifice damage ability")
    void backsUpAnotherCreature() {
        Permanent slinger = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        castVoldarenThrillseeker();
        resolveEtbTargeting(slinger);

        assertThat(slinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        int opponentLife = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int slingerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(slinger);
        harness.activateAbility(player1, slingerIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 4);
        harness.assertInGraveyard(player1, "Bola Slinger");
    }

    @Test
    @DisplayName("Backup targeting Voldaren Thrillseeker only puts the counters on it")
    void backsUpItself() {
        Permanent thrillseeker = castVoldarenThrillseeker();
        resolveEtbTargeting(thrillseeker);

        assertThat(thrillseeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Backup's granted sacrifice damage ability expires at the end of the turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent slinger = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        castVoldarenThrillseeker();
        resolveEtbTargeting(slinger);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        int slingerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(slinger);
        assertThatThrownBy(() -> harness.activateAbility(player1, slingerIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(slinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Thrillseeker can sacrifice itself immediately after entering and uses its counters for damage")
    void sacrificesItselfWithBackupCounters() {
        Permanent thrillseeker = castVoldarenThrillseeker();
        resolveEtbTargeting(thrillseeker);
        int opponentLife = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(thrillseeker);
        harness.activateAbility(player1, index, null, player2.getId());

        harness.assertInGraveyard(player1, "Voldaren Thrillseeker");
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 3);
    }

    @Test
    @DisplayName("An opponent's creature can receive backup and its controller can activate the granted ability")
    void backsUpOpponentsCreature() {
        Permanent slinger = harness.addToBattlefieldAndReturn(player2, new BolaSlinger());
        castVoldarenThrillseeker();
        resolveEtbTargeting(slinger);
        int controllerLife = gd.getLife(player1.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(slinger);
        harness.activateAbility(player2, index, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife - 4);
        harness.assertInGraveyard(player2, "Bola Slinger");
        harness.assertOnBattlefield(player1, "Voldaren Thrillseeker");
    }

    @Test
    @DisplayName("Backup still grants its ability after Thrillseeker is sacrificed in response")
    void backupResolvesAfterSourceLeaves() {
        Permanent slinger = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        Permanent thrillseeker = castVoldarenThrillseeker();
        harness.handlePermanentChosen(player1, slinger.getId());
        int opponentLife = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(thrillseeker);
        harness.activateAbility(player1, sourceIndex, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(slinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        int slingerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(slinger);
        harness.activateAbility(player1, slingerIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 5);
        harness.assertInGraveyard(player1, "Voldaren Thrillseeker");
        harness.assertInGraveyard(player1, "Bola Slinger");
    }

    @Test
    @DisplayName("The sacrifice ability can deal damage to a creature")
    void dealsDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BolaSlinger());
        Permanent thrillseeker = castVoldarenThrillseeker();
        resolveEtbTargeting(thrillseeker);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(thrillseeker);
        harness.activateAbility(player1, index, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Voldaren Thrillseeker");
        harness.assertInGraveyard(player2, "Bola Slinger");
    }

    private Permanent castVoldarenThrillseeker() {
        harness.castFromHand(player1, new VoldarenThrillseeker(), "{2}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Voldaren Thrillseeker");
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
