package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerritorialBruntar.class, Forest.class, GrizzlyBears.class})
class TerritorialBruntarTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall exiles until a nonland and grants normal-cost cast permission this turn")
    void landfallExilesUntilNonlandAndGrantsCastPermission() {
        Forest exiledLand = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new TerritorialBruntar());
        harness.setLibrary(player1, List.of(exiledLand, bears));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactly(exiledLand.getId(), bears.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(bears.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(bears.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Landfall does not trigger for an opponent's land")
    void opponentLandDoesNotTrigger() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new TerritorialBruntar());
        harness.setLibrary(player1, List.of(bears));
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Landfall exiles all remaining cards when no nonland is found")
    void noNonlandCardLeavesNoCastPermission() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.addToBattlefield(player1, new TerritorialBruntar());
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactly(first.getId(), second.getId());
        assertThat(gd.exilePlayPermissions).isEmpty();
    }
}
