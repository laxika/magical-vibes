package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HarmsWay;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XyrisTheWrithingStorm.class, GrizzlyBears.class, HarmsWay.class})
class XyrisTheWrithingStormTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Snake for each opponent draw after the first draw of their draw step")
    void createsSnakeForExtraDraws() {
        harness.addToBattlefield(player1, new XyrisTheWrithingStorm());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);

        draw(player2);
        assertThat(gd.stack).isEmpty();

        draw(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(1);
    }

    @Test
    @DisplayName("Makes both the controller and damaged player draw combat damage amount")
    void bothPlayersDrawCombatDamageAmount() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 20);

        Permanent xyris = addCreatureReady(player1, new XyrisTheWrithingStorm());
        xyris.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(findPermanents(player1, "Snake")).hasSize(3);
    }

    @Test
    @DisplayName("Combat damage draws the exact top cards for both players")
    void combatDamageDrawsEachPlayersTopCards() {
        Card player1Drawn1 = new GrizzlyBears();
        Card player1Drawn2 = new GrizzlyBears();
        Card player1Drawn3 = new GrizzlyBears();
        Card player2Drawn1 = new GrizzlyBears();
        Card player2Drawn2 = new GrizzlyBears();
        Card player2Drawn3 = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(player1Drawn1, player1Drawn2, player1Drawn3));
        harness.setLibrary(player2, List.of(player2Drawn1, player2Drawn2, player2Drawn3));
        harness.setLife(player2, 20);
        addCreatureReady(player1, new XyrisTheWrithingStorm());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(player1Drawn1, player1Drawn2, player1Drawn3);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactly(player2Drawn1, player2Drawn2, player2Drawn3);
    }

    @Test
    @DisplayName("Creates a Snake for the opponent's first draw outside their draw step")
    void createsSnakeForFirstDrawOutsideDrawStep() {
        harness.addToBattlefield(player1, new XyrisTheWrithingStorm());
        harness.setLibrary(player2, List.of(new XyrisTheWrithingStorm()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        draw(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Snake")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create Snakes for its controller's draws")
    void controllerDrawsDoNotCreateSnakes() {
        harness.addToBattlefield(player1, new XyrisTheWrithingStorm());
        harness.setLibrary(player1, List.of(new XyrisTheWrithingStorm(), new XyrisTheWrithingStorm()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        draw(player1);
        draw(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Snake")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent draws during the controller's draw step still create Snakes")
    void opponentDrawDuringControllersDrawStepCreatesSnake() {
        harness.addToBattlefield(player1, new XyrisTheWrithingStorm());
        harness.setLibrary(player2, List.of(new XyrisTheWrithingStorm()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);

        draw(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Snake")).hasSize(1);
    }

    @Test
    @DisplayName("A prior upkeep draw does not consume the draw-step exception")
    void upkeepDrawDoesNotConsumeDrawStepException() {
        harness.addToBattlefield(player1, new XyrisTheWrithingStorm());
        harness.setLibrary(player2, List.of(new XyrisTheWrithingStorm(), new XyrisTheWrithingStorm()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        draw(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Snake")).hasSize(1);

        harness.forceStep(TurnStep.DRAW);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Snake")).hasSize(1);
    }

    @Test
    @DisplayName("Redirected damage from a defending Xyris makes the active player draw first")
    void activePlayerDrawsFirstForDefendingXyris() {
        addCreatureReady(player1, new XyrisTheWrithingStorm());
        Permanent blocker = addCreatureReady(player2, new XyrisTheWrithingStorm());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new XyrisTheWrithingStorm(), new XyrisTheWrithingStorm()));
        harness.setLibrary(player2, List.of(new XyrisTheWrithingStorm(), new XyrisTheWrithingStorm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, blocker.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        String activeDraw = gd.playerIdToName.get(player1.getId()) + " draws a card.";
        String defendingDraw = gd.playerIdToName.get(player2.getId()) + " draws a card.";
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText())
                .filter(text -> text.equals(activeDraw) || text.equals(defendingDraw)).toList())
                .containsExactly(activeDraw, activeDraw, defendingDraw, defendingDraw);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
