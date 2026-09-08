package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieMastermind.class, GrizzlyBears.class})
class FaerieMastermindTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an opponent draws their second card of the turn")
    void drawsOnOpponentsSecondDrawOnlyOnce() {
        harness.addToBattlefield(player1, new FaerieMastermind());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        draw(player2);
        assertThat(gd.stack).isEmpty();

        draw(player2);
        assertThat(gd.stack).hasSize(1);

        draw(player2);
        assertThat(gd.stack).hasSize(1);

        resolveTopOfStack();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger on its controller's draw or an opponent's first draw")
    void doesNotTriggerOnControllerOrFirstOpponentDraw() {
        harness.addToBattlefield(player1, new FaerieMastermind());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        draw(player1);
        draw(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated ability makes each player draw a card")
    void activatedAbilityDrawsForEachPlayer() {
        harness.addToBattlefield(player1, new FaerieMastermind());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
