package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RunedHalo;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfessorDellianFel.class, AngelOfMercy.class, GrizzlyBears.class, RunedHalo.class})
class ProfessorDellianFelTest extends BaseCardTest {

    @Test
    @DisplayName("+2 gains 3 life")
    void plusTwoGainsLife() {
        Permanent professor = addReadyProfessor(player1, 4);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(professor.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("0 draws a card and loses 1 life")
    void zeroDrawsAndLosesLife() {
        Permanent professor = addReadyProfessor(player1, 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(professor.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("-3 destroys a target creature")
    void minusThreeDestroysCreature() {
        Permanent professor = addReadyProfessor(player1, 4);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, bears.getId());
        harness.passBothPriorities();

        assertThat(professor.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-6 creates a life-gain emblem that drains the opponent")
    void minusSixCreatesLifeGainEmblem() {
        addReadyProfessor(player1, 6);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Professor Dellian Fel");

        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    private Permanent addReadyProfessor(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ProfessorDellianFel());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    @Test
    @DisplayName("-3 can destroy its controller's creature")
    void minusThreeCanDestroyOwnCreature() {
        addReadyProfessor(player1, 4);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("-3 cannot target a noncreature planeswalker")
    void minusThreeRejectsNoncreature() {
        Permanent professor = addReadyProfessor(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, professor.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(professor.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only one loyalty ability can be activated per turn")
    void cannotActivateSecondLoyaltyAbility() {
        Permanent professor = addReadyProfessor(player1, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(professor.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The emblem does not trigger when an opponent gains life")
    void emblemIgnoresOpponentLifeGain() {
        addReadyProfessor(player1, 6);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore + 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection from Professor Dellian Fel does not stop the unnamed emblem")
    void emblemIsNotNamedProfessorDellianFel() {
        addReadyProfessor(player1, 6);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        Permanent halo = harness.addToBattlefieldAndReturn(player2, new RunedHalo());
        halo.setChosenName("Professor Dellian Fel");
        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }
}
