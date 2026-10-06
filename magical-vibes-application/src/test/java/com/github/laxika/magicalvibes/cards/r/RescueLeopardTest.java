package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RescueLeopard.class, Forest.class, GrizzlyBears.class})
class RescueLeopardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Rescue Leopard and accepting discards a card and draws a card")
    void tappingSelfAcceptDiscardsAndDraws() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new RescueLeopard());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, List.of(new Forest()));

        tap(leopard);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining Rescue Leopard's trigger does not discard or draw")
    void decliningDoesNothing() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new RescueLeopard());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, List.of(new Forest()));

        tap(leopard);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Rescue Leopard")
    void tappingOtherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new RescueLeopard());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(otherCreature);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting with an empty hand does not draw")
    void emptyHandDoesNotDraw() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new RescueLeopard());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        tap(leopard);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only the tapped Leopard triggers when multiple Leopards are present")
    void onlyTappedLeopardTriggers() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new RescueLeopard());
        harness.addToBattlefield(player1, new RescueLeopard());
        harness.addToBattlefield(player2, new RescueLeopard());

        tap(leopard);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's Leopard rummages for its own controller")
    void opponentsLeopardDiscardsAndDrawsForOpponent() {
        harness.addToBattlefield(player1, new RescueLeopard());
        Permanent leopard = harness.addToBattlefieldAndReturn(player2, new RescueLeopard());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new RescueLeopard()));
        harness.setLibrary(player2, List.of(new Forest()));

        tap(leopard);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Rescue Leopard");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
