package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProtectionRacket.class, GrizzlyBears.class})
class ProtectionRacketTest extends BaseCardTest {

    @Test
    void opponentPaysManaValueToExileRevealedCard() {
        GrizzlyBears bears = new GrizzlyBears();
        prepareUpkeep(bears);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void decliningPaymentPutsRevealedCardIntoHand() {
        GrizzlyBears bears = new GrizzlyBears();
        prepareUpkeep(bears);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bears);
    }

    @Test
    void cannotPayAutomaticallyPutsRevealedCardIntoHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLife(player2, 1);
        prepareUpkeep(bears);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 1);
    }

    @Test
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new ProtectionRacket());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player2, 20);
    }

    private void prepareUpkeep(GrizzlyBears topCard) {
        harness.addToBattlefield(player1, new ProtectionRacket());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
