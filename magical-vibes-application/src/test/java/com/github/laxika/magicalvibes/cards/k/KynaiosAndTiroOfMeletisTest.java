package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KynaiosAndTiroOfMeletis.class, Forest.class, Island.class, GrizzlyBears.class})
class KynaiosAndTiroOfMeletisTest extends BaseCardTest {

    @Test
    void bothPlayersMayPutALandOntoTheBattlefieldAndControllerDraws() {
        Card forest = new Forest();
        Card island = new Island();
        Card controllerDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(forest));
        harness.setHand(player2, List.of(island));
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new KynaiosAndTiroOfMeletis());

        resolveEndStepTrigger();

        chooseLand(player1, forest);
        chooseLand(player2, island);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard().getId())).contains(forest.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getCard().getId())).contains(island.getId());
    }

    @Test
    void opponentWhoDeclinesDrawsAndNoLandEnters() {
        Card controllerDraw = new GrizzlyBears();
        Card opponentDraw = new GrizzlyBears();
        Card forest = new Forest();
        harness.setHand(player1, List.of(forest));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setLibrary(player2, List.of(opponentDraw));
        harness.addToBattlefield(player1, new KynaiosAndTiroOfMeletis());

        resolveEndStepTrigger();

        chooseNoLand(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().getId().equals(forest.getId()))).isTrue();
    }

    private void chooseLand(com.github.laxika.magicalvibes.model.Player player, Card land) {
        PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice choice =
                gd.interaction.activeInteraction(
                        PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player.getId());
        harness.handleMultipleCardsChosen(player, List.of(land.getId()));
    }

    private void chooseNoLand(com.github.laxika.magicalvibes.model.Player player) {
        PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice choice =
                gd.interaction.activeInteraction(
                        PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player.getId());
        harness.handleMultipleCardsChosen(player, List.of());
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
