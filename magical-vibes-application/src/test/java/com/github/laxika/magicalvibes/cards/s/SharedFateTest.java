package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SharedFate.class, Forest.class, AlphaMyr.class})
class SharedFateTest extends BaseCardTest {

    @Test
    @DisplayName("A draw exiles the top card of an opponent's library face down")
    void drawIsReplacedByOpponentLibraryExile() {
        SharedFate source = new SharedFate();
        harness.addToBattlefield(player1, source);
        CardSetup setup = prepareDraw(player1, new AlphaMyr());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(setup.drawerLibraryCard());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(setup.remainingOpponentCard());
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(setup.exiledCard().getId()));

        ExiledCardEntry entry = gd.findExiledCard(setup.exiledCard().getId());
        assertThat(entry.ownerId()).isEqualTo(player2.getId());
        assertThat(entry.sourcePermanentId()).isEqualTo(harness.getPermanentId(player1, "Shared Fate"));
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.exilerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The other player also exiles from an opponent's library when drawing")
    void eachPlayerUsesAnOpponentsLibrary() {
        SharedFate source = new SharedFate();
        harness.addToBattlefield(player1, source);
        CardSetup setup = prepareDraw(player2, new AlphaMyr());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(setup.drawerLibraryCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(setup.remainingOpponentCard());
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(c -> c.getId().equals(setup.exiledCard().getId()));

        ExiledCardEntry entry = gd.findExiledCard(setup.exiledCard().getId());
        assertThat(entry.ownerId()).isEqualTo(player1.getId());
        assertThat(entry.sourcePermanentId()).isEqualTo(harness.getPermanentId(player1, "Shared Fate"));
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.exilerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The player who exiled a card with Shared Fate can cast it normally")
    void drawerCanCastExiledSpell() {
        harness.addToBattlefield(player1, new SharedFate());
        CardSetup setup = prepareDraw(player1, new AlphaMyr());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, setup.exiledCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alpha Myr");
        assertThat(gd.findExiledCard(setup.exiledCard().getId())).isNull();
    }

    @Test
    @DisplayName("The player who did not exile a card cannot cast it with Shared Fate")
    void nonExilerCannotCastExiledSpell() {
        harness.addToBattlefield(player1, new SharedFate());
        CardSetup setup = prepareDraw(player1, new AlphaMyr());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player2, setup.exiledCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    @DisplayName("The player who exiled a land with Shared Fate can play it")
    void drawerCanPlayExiledLand() {
        harness.addToBattlefield(player1, new SharedFate());
        CardSetup setup = prepareDraw(player1, new Forest());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, setup.exiledCard().getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(setup.exiledCard().getId())).isNull();
    }

    @Test
    @DisplayName("Shared Fate's permission ends when the enchantment leaves the battlefield")
    void permissionEndsWhenSourceLeaves() {
        harness.addToBattlefield(player1, new SharedFate());
        CardSetup setup = prepareDraw(player1, new AlphaMyr());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, setup.exiledCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    @DisplayName("An empty opponent library replaces the draw without losing the game")
    void emptyOpponentLibraryExilesNothing() {
        harness.addToBattlefield(player1, new SharedFate());
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(remaining));
        harness.setLibrary(player2, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.winnerPlayerId).isNull();
    }

    private CardSetup prepareDraw(com.github.laxika.magicalvibes.model.Player drawer, Card exiledCard) {
        Card drawerLibraryCard = new Forest();
        Card remainingOpponentCard = new Forest();
        com.github.laxika.magicalvibes.model.Player opponent = drawer.getId().equals(player1.getId())
                ? player2 : player1;
        harness.setLibrary(drawer, List.of(drawerLibraryCard));
        harness.setLibrary(opponent, List.of(exiledCard, remainingOpponentCard));
        return new CardSetup(drawerLibraryCard, remainingOpponentCard, exiledCard);
    }

    private record CardSetup(Card drawerLibraryCard, Card remainingOpponentCard, Card exiledCard) {
    }
}
