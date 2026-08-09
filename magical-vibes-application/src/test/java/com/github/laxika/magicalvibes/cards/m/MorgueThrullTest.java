package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MorgueThrullTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Morgue Thrull mills three cards from its controller's library")
    void sacrificingMillsThreeCards() {
        harness.addToBattlefield(player1, new MorgueThrull());
        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 5) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(deck).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Morgue Thrull");
    }

    @Test
    @DisplayName("Morgue Thrull's sacrifice cost is paid before milling resolves")
    void sacrificeIsPaidOnActivation() {
        harness.addToBattlefield(player1, new MorgueThrull());
        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Morgue Thrull");
        harness.assertInGraveyard(player1, "Morgue Thrull");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Morgue Thrull can be sacrificed while summoning sick")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new MorgueThrull());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Morgue Thrull");
    }
}
