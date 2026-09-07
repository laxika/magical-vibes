package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IrascibleWolverine.class, Forest.class})
class IrascibleWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the top card with end-of-turn play permission")
    void etbExilesTopCardWithPlayPermission() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new IrascibleWolverine()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("ETB exiles nothing when the library is empty")
    void etbExilesNothingWithEmptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new IrascibleWolverine()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).isEmpty();
    }
}
