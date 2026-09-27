package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.p.Pariah;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderworldDreams.class, CounselOfTheSoratami.class, GrizzlyBears.class, Pariah.class,
        PlatinumAngel.class, IvoryMask.class})
class UnderworldDreamsTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        gd.turnNumber = 2; // avoid first-turn draw skip
        advanceToUpkeep(activePlayer);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("Opponent draw step draw causes 1 damage")
    void triggersOnOpponentDrawStepDraw() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.setLife(player2, 20);

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Underworld Dreams trigger

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Controller draw step draw does not trigger Underworld Dreams")
    void doesNotTriggerOnControllerDraw() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.setLife(player1, 20);

        advanceToDraw(player1);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An unsuccessful empty-library draw does not trigger Underworld Dreams")
    void doesNotTriggerWhenOpponentCannotDraw() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.addToBattlefield(player2, new PlatinumAngel());
        harness.setLibrary(player2, List.of());
        harness.setLife(player2, 20);

        advanceToDraw(player2);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent drawing two cards from a spell causes 2 damage")
    void triggersPerCardDrawnFromSpell() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new CounselOfTheSoratami(), "{2}{U}");
        harness.passBothPriorities(); // resolve Counsel of the Soratami
        harness.passBothPriorities(); // resolve first Underworld Dreams trigger
        harness.passBothPriorities(); // resolve second Underworld Dreams trigger

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Two Underworld Dreams cause 2 damage per opponent draw")
    void twoUnderworldDreamsStack() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.setLife(player2, 20);

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Underworld Dreams draw-trigger damage is redirected by Pariah")
    void damageIsRedirectedByPariah() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.setLife(player2, 20);

        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());

        Permanent pariah = harness.addToBattlefieldAndReturn(player2, new Pariah());
        pariah.setAttachedTo(enchantedCreature.getId());

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Underworld Dreams trigger

        harness.assertLife(player2, 20);
        assertThat(gameLogContains("redirected Underworld Dreams damage")).isTrue();
    }

    @Test
    @DisplayName("Opponent shroud does not stop Underworld Dreams damage")
    void damagesOpponentWithShroud() {
        harness.addToBattlefield(player1, new UnderworldDreams());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.setLife(player2, 20);

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Underworld Dreams trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
