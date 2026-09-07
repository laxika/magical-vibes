package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Compulsion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctuarySmasher.class, GrizzlyBears.class, Compulsion.class})
class SanctuarySmasherTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling puts a first strike counter on a target creature you control and draws a card")
    void cyclingPutsFirstStrikeCounterOnOwnCreatureAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SanctuarySmasher()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Sanctuary Smasher");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling cannot target a creature an opponent controls")
    void cyclingCannotTargetOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SanctuarySmasher()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    @Test
    void cyclingWithoutALegalCounterTargetStillDraws() {
        harness.setHand(player1, List.of(new SanctuarySmasher()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingTheCounterTargetDoesNotCounterTheCyclingDraw() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SanctuarySmasher()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void ordinaryDiscardDoesNotPutACounterOnACreature() {
        harness.addToBattlefield(player1, new Compulsion());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SanctuarySmasher()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.FIRST_STRIKE)).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
