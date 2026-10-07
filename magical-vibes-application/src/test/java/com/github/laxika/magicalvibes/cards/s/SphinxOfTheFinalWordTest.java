package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphinxOfTheFinalWord.class, Cancel.class, Divination.class, GrizzlyBears.class, Shock.class})
class SphinxOfTheFinalWordTest extends BaseCardTest {

    @Test
    void itsCreatureSpellCannotBeCountered() {
        SphinxOfTheFinalWord sphinx = new SphinxOfTheFinalWord();

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, sphinx, "{5}{U}{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sphinx.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sphinx of the Final Word");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void controllerInstantAndSorcerySpellsCannotBeCountered() {
        harness.addToBattlefield(player1, new SphinxOfTheFinalWord());

        Divination divination = new Divination();
        Shock firstDraw = new Shock();
        Cancel secondDraw = new Cancel();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, divination, "{2}{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, divination.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divination");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void controllerCreatureSpellsCanBeCountered() {
        harness.addToBattlefield(player1, new SphinxOfTheFinalWord());

        GrizzlyBears bears = new GrizzlyBears();

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opponentInstantAndSorcerySpellsCanBeCountered() {
        harness.addToBattlefield(player1, new SphinxOfTheFinalWord());

        Divination divination = new Divination();
        Shock firstDraw = new Shock();
        Cancel secondDraw = new Cancel();
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));

        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, divination, "{2}{U}");
        harness.passPriority(player2);
        harness.castInstant(player1, 0, divination.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Divination");
        harness.assertInGraveyard(player1, "Cancel");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void opponentCannotTargetItWithSpells() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfTheFinalWord());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void creatureWithoutFlyingOrReachCannotBlockIt() {
        addCreatureReady(player1, new SphinxOfTheFinalWord());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void controllerInstantResolvesDespiteCounterspell() {
        harness.addToBattlefield(player1, new SphinxOfTheFinalWord());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void opponentInstantCanBeCountered() {
        harness.addToBattlefield(player1, new SphinxOfTheFinalWord());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Cancel");
    }

    @Test
    void sphinxOnStackDoesNotProtectOtherSpells() {
        SphinxOfTheFinalWord sphinx = new SphinxOfTheFinalWord();
        harness.castFromHand(player1, sphinx, "{5}{U}{U}");
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertOnBattlefield(player1, "Sphinx of the Final Word");
    }

    @Test
    void protectionEndsWhenSphinxLeavesBeforeCounterspellResolves() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfTheFinalWord());
        Divination divination = new Divination();
        Shock firstDraw = new Shock();
        Cancel secondDraw = new Cancel();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.castFromHand(player1, divination, "{2}{U}");
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, divination.getId());

        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0, sphinx.getId());
        }
        harness.assertNotOnBattlefield(player1, "Sphinx of the Final Word");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Divination");
        harness.assertInGraveyard(player1, "Sphinx of the Final Word");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }
}
