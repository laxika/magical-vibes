package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnjeFalkenrath.class, AsylumVisitor.class, GrizzlyBears.class})
class AnjeFalkenrathTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and discarding draws a card")
    void tapsDiscardsAndDraws() {
        Permanent anje = addReadyAnje();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new AsylumVisitor()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveStack();

        assertThat(anje.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Asylum Visitor");
    }

    @Test
    @DisplayName("Discarding a card with madness untaps Anje Falkenrath")
    void discardingMadnessUntapsAnje() {
        Permanent anje = addReadyAnje();
        harness.setHand(player1, List.of(new AsylumVisitor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        declineMadnessIfOffered();
        resolveStack();

        assertThat(anje.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Asylum Visitor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyAnje() {
        Permanent anje = harness.addToBattlefieldAndReturn(player1, new AnjeFalkenrath());
        anje.setSummoningSick(false);
        return anje;
    }

    private void declineMadnessIfOffered() {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, false);
            }
        }
    }
}
