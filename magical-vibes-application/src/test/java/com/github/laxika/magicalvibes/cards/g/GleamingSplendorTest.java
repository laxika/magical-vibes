package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GleamingSplendor.class, GrizzlyBears.class})
class GleamingSplendorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when an opponent draws their second card each turn")
    void createsTreasureOnOpponentsSecondDraw() {
        harness.addToBattlefield(player1, new GleamingSplendor());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        assertThat(gd.stack).isEmpty();
        draw(player2);
        assertThat(gd.stack).hasSize(1);

        resolveTopOfStack();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        draw(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Makes two different targeted players each draw a card")
    void targetedPlayersEachDraw() {
        harness.addToBattlefield(player1, new GleamingSplendor());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 1);
    }

    @Test
    @DisplayName("Does not allow the same player to be targeted twice")
    void cannotTargetSamePlayerTwice() {
        harness.addToBattlefield(player1, new GleamingSplendor());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
