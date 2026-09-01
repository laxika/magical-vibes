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
}
