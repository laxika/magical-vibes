package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
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

@CardUsed({AnjeFalkenrath.class, AsylumVisitor.class, GrizzlyBears.class, FaithlessLooting.class})
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
        return addCreatureReady(player1, new AnjeFalkenrath());
    }

    @Test
    void hasteAllowsActivationWhileSummoningSick() {
        Permanent anje = harness.addToBattlefieldAndReturn(player1, new AnjeFalkenrath());
        anje.setSummoningSick(true);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new AsylumVisitor()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveStack();

        assertThat(anje.isTapped()).isTrue();
        harness.assertInHand(player1, "Asylum Visitor");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        Permanent anje = addReadyAnje();
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(anje.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent anje = addReadyAnje();
        anje.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void madnessDiscardFromAnotherSourceUntapsOnlyControllersAnje() {
        Permanent anje = addReadyAnje();
        Permanent opposingAnje = harness.addToBattlefieldAndReturn(player2, new AnjeFalkenrath());
        anje.tap();
        opposingAnje.tap();
        harness.setHand(player1, List.of(new FaithlessLooting(), new AsylumVisitor(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);
        declineMadnessIfOffered();
        resolveStack();

        assertThat(anje.isTapped()).isFalse();
        assertThat(opposingAnje.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Asylum Visitor");
    }

    private void declineMadnessIfOffered() {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            resolveAllTriggers();
            declineMadnessIfOffered();
        }
    }
}
