package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormfistCrusader.class, Gingerbrute.class, ScorchingDragonfire.class})
class StormfistCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, each player draws a card and loses 1 life")
    void eachPlayerDrawsAndLosesLifeDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new StormfistCrusader());
        Gingerbrute player1Draw = new Gingerbrute();
        Gingerbrute player2Draw = new Gingerbrute();
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Draw);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new StormfistCrusader());
        Gingerbrute player1Draw = new Gingerbrute();
        Gingerbrute player2Draw = new Gingerbrute();
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(player2Draw);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
    @Test
    void multipleCrusadersEachTriggerSeparately() {
        harness.addToBattlefield(player1, new StormfistCrusader());
        harness.addToBattlefield(player1, new StormfistCrusader());
        Gingerbrute firstDraw = new Gingerbrute();
        Gingerbrute secondDraw = new Gingerbrute();
        Gingerbrute opponentFirstDraw = new Gingerbrute();
        Gingerbrute opponentSecondDraw = new Gingerbrute();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(opponentFirstDraw, opponentSecondDraw));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentFirstDraw, opponentSecondDraw);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void upkeepTriggerResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new StormfistCrusader());
        Gingerbrute controllerDraw = new Gingerbrute();
        Gingerbrute opponentDraw = new Gingerbrute();
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setLibrary(player2, List.of(opponentDraw));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new ScorchingDragonfire()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Stormfist Crusader"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Stormfist Crusader");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentDraw);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new StormfistCrusader());
        addCreatureReady(player2, new Gingerbrute());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new StormfistCrusader());
        Permanent first = addCreatureReady(player2, new Gingerbrute());
        Permanent second = addCreatureReady(player2, new Gingerbrute());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void bothPlayersDrawBeforeLethalLifeLossEndsGameInDraw() {
        harness.addToBattlefield(player1, new StormfistCrusader());
        Gingerbrute controllerDraw = new Gingerbrute();
        Gingerbrute opponentDraw = new Gingerbrute();
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setLibrary(player2, List.of(opponentDraw));
        harness.setLife(player1, 1);
        harness.setLife(player2, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentDraw);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
    }
}
