package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MilitaryIntelligence.class, RuneclawBear.class})
class MilitaryIntelligenceTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when two creatures attack")
    void drawsWhenTwoCreaturesAttack() {
        setUpBattlefieldAndLibrary();

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when fewer than two creatures attack")
    void doesNotDrawWhenFewerThanTwoCreaturesAttack() {
        setUpBattlefieldAndLibrary();

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Three attackers still draw only one card")
    void drawsOnlyOnceForThreeAttackers() {
        setUpBattlefieldAndLibrary();
        addCreatureReady(player1, new RuneclawBear());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear()));

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponent attacks do not trigger the enchantment")
    void doesNotDrawForOpponentAttackers() {
        setUpBattlefieldAndLibrary();
        addCreatureReady(player2, new RuneclawBear());
        addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player2, List.of());

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw resolves after attackers and the enchantment leave")
    void drawDoesNotRecheckAttackersOrRequireSource() {
        setUpBattlefieldAndLibrary();

        declareAttackers(List.of(1, 2));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each copy triggers independently")
    void twoCopiesDrawTwoCards() {
        setUpBattlefieldAndLibrary();
        harness.addToBattlefield(player1, new MilitaryIntelligence());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear()));

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void setUpBattlefieldAndLibrary() {
        harness.addToBattlefield(player1, new MilitaryIntelligence());
        addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear()));
    }
}
