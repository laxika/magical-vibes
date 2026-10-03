package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CathedralAcolyte.class, GrizzlyBears.class, HillGiant.class, ProdigalPyromancer.class, Shock.class})
class CathedralAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Gives ward {1} to your creatures with any counter")
    void givesWardToYourCounteredCreatures() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent protectedCreature = addCreatureReady(player1, new HillGiant());
        protectedCreature.setCounterCount(CounterType.CHARGE, 1);
        Permanent unprotectedCreature = addCreatureReady(player1, new HillGiant());

        castShockFromOpponent(protectedCreature);
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");

        castShockFromOpponent(unprotectedCreature);
        assertThat(unprotectedCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Taps to put a +1/+1 counter on a creature that entered this turn")
    void putsCounterOnCreatureEnteredThisTurn() {
        Permanent acolyte = addCreatureReady(player1, new CathedralAcolyte());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(acolyte.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature that entered before this turn")
    void cannotTargetCreatureFromEarlierTurn() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn");
    }

    @Test
    void protectsItselfWhenItHasACounter() {
        Permanent acolyte = addCreatureReady(player1, new CathedralAcolyte());
        acolyte.setCounterCount(CounterType.CHARGE, 1);

        castShockFromOpponent(acolyte);

        harness.assertOnBattlefield(player1, "Cathedral Acolyte");
        assertThat(acolyte.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void opponentCanPayOneManaToResolveSpell() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        castShockFromOpponent(target);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void opponentCanDeclineWardPayment() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        castShockFromOpponent(target);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void countersOpponentActivatedAbility() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(pyromancer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantWardToOpponentsCreatures() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void wardDoesNotCounterControllersOwnSpell() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void wardIsLostWhenLastCounterIsRemoved() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setCounterCount(CounterType.CHARGE, 1);
        castShockFromOpponent(target);
        assertThat(target.getMarkedDamage()).isZero();
        target.setCounterCount(CounterType.CHARGE, 0);

        castShockFromOpponent(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void wardIsLostWhenAcolyteLeavesBattlefield() {
        Permanent acolyte = addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setCounterCount(CounterType.CHARGE, 1);

        castShockFromOpponent(acolyte);
        harness.assertInGraveyard(player1, "Cathedral Acolyte");
        castShockFromOpponent(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void twoAcolytesRequireTwoSeparateWardPayments() {
        addCreatureReady(player1, new CathedralAcolyte());
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        castShockFromOpponent(target);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canPutCounterOnOpponentsCreatureThatEnteredThisTurn() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void creatureGainsWardAfterActivatedAbilityAddsCounter() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        castShockFromOpponent(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    private void castShockFromOpponent(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
