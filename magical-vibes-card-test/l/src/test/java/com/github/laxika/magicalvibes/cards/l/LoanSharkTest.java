package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoanShark.class, Ornithopter.class, GrizzlyBears.class})
class LoanSharkTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when you have cast two or more spells this turn")
    void drawsAfterTwoSpells() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Ornithopter(), new Ornithopter(), new LoanShark()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw when fewer than two spells were cast this turn")
    void doesNotDrawBeforeThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LoanShark()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void countsItselfAsTheSecondSpell() {
        harness.setLibrary(player1, List.of(new LoanShark()));
        harness.setHand(player1, List.of(new LoanShark(), new LoanShark()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Loan Shark");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void plottingDoesNotCountAsCastingASpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LoanShark(), new LoanShark()));
        harness.setLibrary(player1, List.of(new LoanShark()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Loan Shark");
    }

    @Test
    void plottedCardCanBeCastForFreeOnlyOnALaterTurnAtSorcerySpeed() {
        LoanShark shark = new LoanShark();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(shark));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Loan Shark");
        harness.assertNotOnBattlefield(player1, "Loan Shark");
        assertThatThrownBy(() -> harness.castFromExile(player1, shark.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setLibrary(player1, List.of(new LoanShark(), new LoanShark(), new LoanShark()));
        harness.setLibrary(player2, List.of(new LoanShark(), new LoanShark(), new LoanShark()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, shark.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, shark.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loan Shark");
        assertThat(gd.stack).isEmpty();
    }
}
