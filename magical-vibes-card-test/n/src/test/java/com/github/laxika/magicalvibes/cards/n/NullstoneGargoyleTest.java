package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NullstoneGargoyle.class, BorosSignet.class, ElvesOfDeepShadow.class})
class NullstoneGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Counters only the first noncreature spell of the turn across all players")
    void countersFirstNoncreatureSpellGlobally() {
        harness.addToBattlefield(player1, new NullstoneGargoyle());

        castBorosSignet(player2);
        harness.assertInGraveyard(player2, "Boros Signet");

        castBorosSignet(player1);
        harness.assertOnBattlefield(player1, "Boros Signet");
    }

    @Test
    @DisplayName("Creature spells do not count as the first noncreature spell")
    void ignoresCreatureSpells() {
        harness.addToBattlefield(player1, new NullstoneGargoyle());
        prepareCast(player2);

        harness.castFromHand(player2, new ElvesOfDeepShadow(), "{G}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Elves of Deep Shadow");

        castBorosSignet(player2);
        harness.assertInGraveyard(player2, "Boros Signet");
    }

    @Test
    @DisplayName("A spell cast before Nullstone Gargoyle enters still counts")
    void spellsBeforeEntryCount() {
        castBorosSignet(player2);
        harness.assertOnBattlefield(player2, "Boros Signet");

        harness.addToBattlefield(player1, new NullstoneGargoyle());
        castBorosSignet(player2);

        assertThat(findPermanents(player2, "Boros Signet")).hasSize(2);
    }

    @Test
    @DisplayName("Counters the first noncreature spell of each turn")
    void resetsForNextTurn() {
        harness.addToBattlefield(player1, new NullstoneGargoyle());

        castBorosSignet(player1);
        harness.assertInGraveyard(player1, "Boros Signet");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        castBorosSignet(player2);
        harness.assertInGraveyard(player2, "Boros Signet");
    }

    @Test
    @DisplayName("The counter trigger resolves even after Gargoyle leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        var gargoyle = harness.addToBattlefieldAndReturn(player1, new NullstoneGargoyle());
        prepareCast(player2);
        harness.castFromHand(player2, new BorosSignet(), "{2}");

        gd.playerBattlefields.get(player1.getId()).remove(gargoyle);
        gd.playerGraveyards.get(player1.getId()).add(gargoyle.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boros Signet");
        harness.assertNotOnBattlefield(player2, "Boros Signet");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple Gargoyles trigger for the same first spell and ignore later spells")
    void multipleGargoylesCounterOnlyTheSameFirstSpell() {
        harness.addToBattlefield(player1, new NullstoneGargoyle());
        harness.addToBattlefield(player2, new NullstoneGargoyle());
        prepareCast(player1);
        harness.castFromHand(player1, new BorosSignet(), "{2}");

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.assertInGraveyard(player1, "Boros Signet");
        assertThat(gd.stack).isEmpty();

        castBorosSignet(player2);
        harness.assertOnBattlefield(player2, "Boros Signet");
    }

    private void castBorosSignet(Player player) {
        prepareCast(player);
        harness.castFromHand(player, new BorosSignet(), "{2}");
        harness.passBothPriorities();
    }

    private void prepareCast(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
