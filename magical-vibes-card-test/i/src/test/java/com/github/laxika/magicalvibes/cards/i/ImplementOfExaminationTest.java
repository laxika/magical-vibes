package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturalObsolescence;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImplementOfExamination.class, GrizzlyBears.class, Shatter.class, NaturalObsolescence.class})
class ImplementOfExaminationTest extends BaseCardTest {

    @Test
    @DisplayName("Activating it draws a card")
    void activatingItDrawsACard() {
        harness.addToBattlefield(player1, new ImplementOfExamination());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        harness.assertInGraveyard(player1, "Implement of Examination");
    }

    @Test
    @DisplayName("Cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new ImplementOfExamination());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Implement of Examination");
    }

    @Test
    @DisplayName("Draws a card when it is put into a graveyard from the battlefield")
    void drawsWhenPutIntoGraveyardFromBattlefield() {
        harness.addToBattlefield(player1, new ImplementOfExamination());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        var targetId = harness.getPermanentId(player1, "Implement of Examination");
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and each draw resolves separately")
    void sacrificeIsImmediateAndDrawsResolveSeparately() {
        harness.addToBattlefield(player1, new ImplementOfExamination());
        harness.setLibrary(player1, List.of(new ImplementOfExamination(), new ImplementOfExamination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Implement of Examination");
        harness.assertInGraveyard(player1, "Implement of Examination");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Both draws go to the controller even when another player owns the artifact")
    void drawsForControllerRatherThanOwner() {
        ImplementOfExamination implement = new ImplementOfExamination();
        implement.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, implement);
        harness.setLibrary(player2, List.of(new ImplementOfExamination(), new ImplementOfExamination()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        int ownerHandSize = gd.playerHands.get(player1.getId()).size();
        int controllerHandSize = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Implement of Examination");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownerHandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandSize + 2);
    }

    @Test
    @DisplayName("Moving to a library does not trigger a draw")
    void movingToLibraryDoesNotDraw() {
        harness.addToBattlefield(player1, new ImplementOfExamination());
        harness.setHand(player2, List.of(new NaturalObsolescence()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Implement of Examination"));

        harness.assertNotOnBattlefield(player1, "Implement of Examination");
        harness.assertNotInGraveyard(player1, "Implement of Examination");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }
}
