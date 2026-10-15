package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CallOfTheHerd;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Poxwalkers.class, CallOfTheHerd.class, GrizzlyBears.class})
class PoxwalkersTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard tapped when a spell is cast from outside the hand")
    void returnsFromGraveyardForNonHandCast() {
        harness.setGraveyard(player1, List.of(new Poxwalkers(), new CallOfTheHerd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 1);
        harness.passBothPriorities();

        Permanent poxwalkers = findPermanent(player1, "Poxwalkers");
        assertThat(poxwalkers.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Poxwalkers");
    }

    @Test
    @DisplayName("Does not return when a spell is cast from hand")
    void doesNotReturnForHandCast() {
        harness.setGraveyard(player1, List.of(new Poxwalkers()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Poxwalkers");
    }

    @Test
    @DisplayName("An opponent's flashback spell does not return Poxwalkers")
    void doesNotReturnForOpponentCast() {
        harness.setGraveyard(player1, List.of(new Poxwalkers()));
        harness.setGraveyard(player2, List.of(new CallOfTheHerd()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castFlashback(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Poxwalkers");
        harness.assertNotOnBattlefield(player1, "Poxwalkers");
        harness.assertOnBattlefield(player2, "Elephant");
    }

    @Test
    @DisplayName("Each graveyard copy returns only itself when its own trigger resolves")
    void multipleCopiesReturnSeparately() {
        harness.setGraveyard(player1, List.of(new Poxwalkers(), new Poxwalkers(), new CallOfTheHerd()));
        harness.setGraveyard(player2, List.of(new Poxwalkers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Poxwalkers")).hasSize(1);
        harness.assertInGraveyard(player1, "Poxwalkers");
        harness.assertNotOnBattlefield(player1, "Elephant");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Poxwalkers"))
                .hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
        harness.assertNotInGraveyard(player1, "Poxwalkers");
        harness.assertInGraveyard(player2, "Poxwalkers");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Elephant");
    }

    @Test
    @DisplayName("Casting outside the hand does not tap Poxwalkers already on the battlefield")
    void battlefieldCopyDoesNotTrigger() {
        Permanent poxwalkers = harness.addToBattlefieldAndReturn(player1, new Poxwalkers());
        harness.setGraveyard(player1, List.of(new CallOfTheHerd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elephant");
        assertThat(findPermanents(player1, "Poxwalkers")).containsExactly(poxwalkers);
        assertThat(poxwalkers.isTapped()).isFalse();
    }
}
