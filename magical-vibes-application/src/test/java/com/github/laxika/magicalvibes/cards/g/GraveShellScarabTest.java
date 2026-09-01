package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveShellScarab.class, Forest.class})
class GraveShellScarabTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Grave-Shell Scarab draws a card")
    void sacrificesSelfAndDrawsCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new GraveShellScarab());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Grave-Shell Scarab");
        harness.assertInGraveyard(player1, "Grave-Shell Scarab");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("Dredging Grave-Shell Scarab mills one card and returns it to hand")
    void dredgesIntoHand() {
        GraveShellScarab scarab = new GraveShellScarab();
        Forest milled = new Forest();
        harness.setGraveyard(player1, List.of(scarab));
        harness.setLibrary(player1, List.of(milled, new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(scarab);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
    }
}
