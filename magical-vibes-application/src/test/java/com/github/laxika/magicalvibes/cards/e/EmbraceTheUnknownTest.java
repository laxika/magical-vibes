package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmbraceTheUnknown.class, Forest.class, GrizzlyBears.class})
class EmbraceTheUnknownTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top two cards and grants permission to play them")
    void exilesTopTwoCardsAndGrantsPlayPermission() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new EmbraceTheUnknown()));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd)
                .containsEntry(first.getId(), gd.turnNumber + 2)
                .containsEntry(second.getId(), gd.turnNumber + 2);
    }

    @Test
    @DisplayName("Retrace discards a land and returns Embrace the Unknown to the graveyard")
    void retraceDiscardsLandAndReturnsToGraveyard() {
        Card embrace = new EmbraceTheUnknown();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of(embrace));
        harness.setHand(player1, List.of(land));
        addMana();

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(embrace, land);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
