package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuezaAugurOfAgonies.class, GrizzlyBears.class})
class QuezaAugurOfAgoniesTest extends BaseCardTest {

    @Test
    @DisplayName("When you draw a card, target opponent loses 1 life and you gain 1 life")
    void drainsTargetOpponentWhenControllerDraws() {
        harness.addToBattlefield(player1, new QuezaAugurOfAgonies());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        draw(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The draw trigger cannot target its controller")
    void drawTriggerCannotTargetController() {
        harness.addToBattlefield(player1, new QuezaAugurOfAgonies());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        draw(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent drawing a card does not trigger Queza")
    void opponentDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new QuezaAugurOfAgonies());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        draw(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    @Test
    @DisplayName("Drawing two cards produces two separate drain triggers")
    void triggersForEachCardDrawn() {
        harness.addToBattlefield(player1, new QuezaAugurOfAgonies());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new QuezaAugurOfAgonies(), new QuezaAugurOfAgonies()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Removing Queza does not stop a trigger already on the stack")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        var queza = harness.addToBattlefieldAndReturn(player1, new QuezaAugurOfAgonies());
        harness.setLibrary(player1, List.of(new QuezaAugurOfAgonies()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        draw(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, queza));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Queza, Augur of Agonies");
    }

    @Test
    @DisplayName("Queza drains the opponent of its current controller")
    void playerTwoControllerGainsLifeAndTargetsPlayerOne() {
        harness.addToBattlefield(player2, new QuezaAugurOfAgonies());
        harness.setLibrary(player2, List.of(new QuezaAugurOfAgonies()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        draw(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }
}
