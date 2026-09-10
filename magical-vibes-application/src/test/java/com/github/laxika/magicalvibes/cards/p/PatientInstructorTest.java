package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatientInstructor.class, Forest.class, GrizzlyBears.class})
class PatientInstructorTest extends BaseCardTest {

    @Test
    @DisplayName("Recruit creates a Soldier after discarding a nonland card")
    void recruitCreatesSoldierForNonlandDiscard() {
        castAndResolve(new GrizzlyBears(), new Forest());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Recruit does not create a Soldier after discarding a land card")
    void recruitDoesNotCreateSoldierForLandDiscard() {
        castAndResolve(new Forest(), new GrizzlyBears());

        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private void castAndResolve(Card discardedCard, Card drawnCard) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new PatientInstructor(), discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }
    }
}
