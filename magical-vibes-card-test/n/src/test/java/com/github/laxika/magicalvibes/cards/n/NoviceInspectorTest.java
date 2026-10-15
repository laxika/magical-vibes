package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoviceInspector.class})
class NoviceInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("When Novice Inspector enters, its controller investigates")
    void etbCreatesAClueToken() {
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigate waits for the enters trigger to resolve")
    void investigateUsesTheStackAfterTheCreatureResolves() {
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.assertNotOnBattlefield(player1, "Clue");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Novice Inspector");
        harness.assertNotOnBattlefield(player1, "Clue");

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Clue");
    }

    @Test
    @DisplayName("A Clue is sacrificed as a cost and draws only when its ability resolves")
    void clueSacrificeDrawsOneCardOnResolution() {
        NoviceInspector libraryCard = new NoviceInspector();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.enterBattlefieldAndReturn(player1, new NoviceInspector());
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Novice Inspector");
    }

    @Test
    @DisplayName("An Inspector entering without being cast gives its controller the Clue")
    void enteringUnderOtherPlayersControlInvestigatesForThatPlayer() {
        harness.enterBattlefieldAndReturn(player2, new NoviceInspector());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Clue");
    }
}
