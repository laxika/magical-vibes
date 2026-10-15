package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.SenuKeenEyedProtector;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({ArbaazMir.class, Spellbook.class, GrizzlyBears.class, SenuKeenEyedProtector.class})
class ArbaazMirTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry deals 1 damage to each opponent and gains 1 life")
    void ownEntryTriggers() {
        harness.enterBattlefieldAndReturn(player1, new ArbaazMir());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Another nontoken historic permanent entering triggers the ability")
    void anotherNontokenHistoricEntryTriggers() {
        harness.addToBattlefield(player1, new ArbaazMir());

        harness.enterBattlefieldAndReturn(player1, new Spellbook());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A token historic permanent entering does not trigger the ability")
    void tokenHistoricEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArbaazMir());
        Card tokenArtifact = new Spellbook();
        tokenArtifact.setToken(true);

        harness.enterBattlefieldAndReturn(player1, tokenArtifact);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A non-historic permanent entering does not trigger the ability")
    void nonHistoricEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArbaazMir());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's historic permanent entering does not trigger the ability")
    void opponentHistoricEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArbaazMir());

        harness.enterBattlefieldAndReturn(player2, new Spellbook());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A token copy of Arbaaz Mir triggers for its own entry")
    void tokenOwnEntryTriggers() {
        Card tokenArbaaz = new ArbaazMir();
        tokenArbaaz.setToken(true);

        harness.enterBattlefieldAndReturn(player1, tokenArbaaz);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A nontoken legendary creature is historic without being an artifact")
    void legendaryCreatureEntryTriggers() {
        harness.addToBattlefield(player1, new ArbaazMir());

        harness.enterBattlefieldAndReturn(player1, new SenuKeenEyedProtector());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Another legendary token does not trigger Arbaaz Mir")
    void legendaryTokenEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArbaazMir());
        Card tokenSenu = new SenuKeenEyedProtector();
        tokenSenu.setToken(true);

        harness.enterBattlefieldAndReturn(player1, tokenSenu);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A token Arbaaz Mir still triggers for another nontoken historic permanent")
    void tokenSourceTriggersForNontokenHistoricEntry() {
        Card tokenArbaaz = new ArbaazMir();
        tokenArbaaz.setToken(true);
        harness.addToBattlefield(player1, tokenArbaaz);

        harness.enterBattlefieldAndReturn(player1, new SenuKeenEyedProtector());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
