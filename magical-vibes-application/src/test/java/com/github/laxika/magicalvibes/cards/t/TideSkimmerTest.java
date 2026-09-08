package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TideSkimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when two creatures with flying attack")
    void drawsWhenTwoFlyingCreaturesAttack() {
        setUpBattlefieldAndLibrary();
        addCreatureReady(player1, new WindDrake());
        addCreatureReady(player1, new WindDrake());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when only one attacking creature has flying")
    void doesNotDrawWhenOnlyOneAttackerHasFlying() {
        setUpBattlefieldAndLibrary();
        addCreatureReady(player1, new WindDrake());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when two nonflying creatures attack")
    void doesNotDrawWhenNoAttackerHasFlying() {
        setUpBattlefieldAndLibrary();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void setUpBattlefieldAndLibrary() {
        harness.addToBattlefield(player1, new TideSkimmer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));
    }
}
