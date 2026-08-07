package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UbaMaskTest extends BaseCardTest {

    @Test
    @DisplayName("A draw is replaced — the card is exiled with a play permission for the drawer")
    void drawIsReplacedByExile() {
        harness.addToBattlefield(player1, new UbaMask());
        Card top = new GrizzlyBears();
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(top, new Forest())));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    @DisplayName("The opponent's draw is replaced too, and the permission is theirs")
    void opponentDrawIsAlsoReplaced() {
        harness.addToBattlefield(player1, new UbaMask());
        Card top = new GrizzlyBears();
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(top, new Forest())));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The exiled card can be cast that turn")
    void exiledCardCanBeCast() {
        harness.addToBattlefield(player1, new UbaMask());
        Card top = new GrizzlyBears();
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(top, new Forest())));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty library exiles nothing and does not lose the game")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new UbaMask());
        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.winnerPlayerId).isNull();
    }
}
