package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EruthTormentedProphet.class, Forest.class, Mountain.class})
class EruthTormentedProphetTest extends BaseCardTest {

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    @Test
    @DisplayName("Replaces a draw by exiling the top two cards with play permission")
    void replacesDrawWithTopTwoExileAndPlayPermission() {
        Forest first = new Forest();
        Mountain second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new EruthTormentedProphet());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveDraw();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Exiles the available card when the library has fewer than two cards")
    void replacesDrawWithShortLibrary() {
        Forest only = new Forest();
        harness.setLibrary(player1, List.of(only));
        harness.addToBattlefield(player1, new EruthTormentedProphet());

        resolveDraw();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not lose the game when replacing a draw from an empty library")
    void replacesEmptyLibraryDrawWithoutLoss() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new EruthTormentedProphet());

        resolveDraw();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
