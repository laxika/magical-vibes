package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfRunes.class})
class WallOfRunesTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldStartsScryOne() {
        playWallOfRunes(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    void scryOneCanPutTheTopCardOnTheBottom() {
        playWallOfRunes(player1);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
    }

    @Test
    void scryOneCanKeepTheTopCardWithoutChangingLibraryOrder() {
        Card top = new WallOfRunes();
        Card second = new WallOfRunes();
        harness.setLibrary(player1, List.of(top, second));
        playWallOfRunes(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scryWithAnEmptyLibraryResolvesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        playWallOfRunes(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentControllerScriesTheirOwnLibrary() {
        Card top = new WallOfRunes();
        Card second = new WallOfRunes();
        Card otherPlayersTop = new WallOfRunes();
        harness.setLibrary(player2, List.of(top, second));
        harness.setLibrary(player1, List.of(otherPlayersTop));
        playWallOfRunes(player2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherPlayersTop);
    }

    @Test
    void defenderPreventsAttackingAfterSummoningSicknessEnds() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfRunes());
        wall.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThat(als.canAttack(gd, wall, player1.getId())).isFalse();
    }

    private void playWallOfRunes(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new WallOfRunes(), "{U}");
    }
}
