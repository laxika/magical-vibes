package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverwhelmingInstinct.class, GrizzlyBears.class})
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

    private void setUpBattlefieldAndLibrary() {
        harness.addToBattlefield(player1, new OverwhelmingInstinct());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));
    }
}
