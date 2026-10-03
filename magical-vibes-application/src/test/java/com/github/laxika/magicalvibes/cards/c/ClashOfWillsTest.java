package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClashOfWills.class, GrizzlyBears.class, Forest.class})
class ClashOfWillsTest extends BaseCardTest {

    private GrizzlyBears prepareCounterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        return bears;
    }

    @Test
    @DisplayName("Counters the spell when its controller cannot pay X")
    void countersWhenCannotPay() {
        GrizzlyBears bears = prepareCounterTarget();

        harness.setHand(player2, List.of(new ClashOfWills()));
        harness.addMana(player2, ManaColor.BLUE, 4); // {U} + X=3

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, bears.getId()); // X = 3
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters the spell when its controller declines to pay X")
    void countersWhenDeclines() {
        GrizzlyBears bears = prepareCounterTarget();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new ClashOfWills()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId()); // X = 1
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Paying X keeps the spell on the stack")
    void payingKeepsSpell() {
        GrizzlyBears bears = prepareCounterTarget();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new ClashOfWills()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId()); // X = 1
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");

        harness.passBothPriorities(); // resolve Grizzly Bears
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("X of zero counters the spell only if its controller declines to pay nothing")
    void xZeroIsFreeToPay() {
        GrizzlyBears bears = prepareCounterTarget();

        harness.setHand(player2, List.of(new ClashOfWills()));
        harness.addMana(player2, ManaColor.BLUE, 1); // {U} + X=0

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, bears.getId()); // X = 0
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // pay {0}

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining to pay zero still counters the spell")
    void decliningZeroCountersSpell() {
        GrizzlyBears bears = prepareCounterTarget();
        harness.setHand(player2, List.of(new ClashOfWills()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A larger X can be paid with a mixture of mana colors")
    void paysLargerXWithMixedMana() {
        GrizzlyBears bears = prepareCounterTarget();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new ClashOfWills()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller can activate a mana ability to pay during resolution")
    void offersPaymentWithUntappedManaSource() {
        GrizzlyBears bears = prepareCounterTarget();
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player2, List.of(new ClashOfWills()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
