package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DisorderInTheCourt.class, GrizzlyBears.class, FountainOfYouth.class})
class DisorderInTheCourtTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles X creatures, investigates X times, and returns them tapped at the next end step")
    void exilesInvestigatesAndReturnsTapped() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondOwnCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(2);

        harness.castInstantForX(player1, 0, 2,
                List.of(ownCreature.getId(), secondOwnCreature.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).hasSize(2);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("X=0 investigates zero times and exiles no creatures")
    void xZeroDoesNothing() {
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(0);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(fountain.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void addMana(int x) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        if (x > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, x);
        }
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
