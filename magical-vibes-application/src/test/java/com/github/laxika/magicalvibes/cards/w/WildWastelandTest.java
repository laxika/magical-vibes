package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildWasteland.class, Forest.class, Mountain.class})
class WildWastelandTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, exiles the top two cards with play permission")
    void exilesTopTwoCardsWithPlayPermission() {
        Card first = new Forest();
        Card second = new Mountain();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.addToBattlefield(player1, new WildWasteland());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Skips the controller's draw step")
    void skipsControllersDrawStep() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Forest()));
        harness.addToBattlefield(player1, new WildWasteland());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        int handSizeAfterUpkeep = gd.playerHands.get(player1.getId()).size();
        int librarySizeAfterUpkeep = gd.playerDecks.get(player1.getId()).size();

        // Advance through the draw step.
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterUpkeep);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeAfterUpkeep);
    }
}
