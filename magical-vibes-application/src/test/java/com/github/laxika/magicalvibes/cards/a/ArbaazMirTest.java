package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArbaazMir.class, Spellbook.class, GrizzlyBears.class})
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

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
