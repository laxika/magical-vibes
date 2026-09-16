package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverwhelmingInstinct.class, ElvishWarrior.class})
class OverwhelmingInstinctTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when three creatures attack")
    void drawsWhenThreeCreaturesAttack() {
        setUpBattlefieldAndLibrary();

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when fewer than three creatures attack")
    void doesNotDrawWhenFewerThanThreeCreaturesAttack() {
        setUpBattlefieldAndLibrary();

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws only one card when four creatures attack")
    void drawsOnlyOneCardWhenFourCreaturesAttack() {
        setUpBattlefieldAndLibrary(4, new ElvishWarrior(), new ElvishWarrior());

        declareAttackers(List.of(1, 2, 3, 4));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void setUpBattlefieldAndLibrary() {
        setUpBattlefieldAndLibrary(3, new ElvishWarrior());
    }

    private void setUpBattlefieldAndLibrary(int creatureCount, Card... libraryCards) {
        harness.addToBattlefield(player1, new OverwhelmingInstinct());
        for (int i = 0; i < creatureCount; i++) {
            addCreatureReady(player1, new ElvishWarrior());
        }
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCards));
    }
}
